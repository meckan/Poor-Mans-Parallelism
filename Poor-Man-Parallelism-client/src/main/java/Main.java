import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Main {

    /**
     * Main commandline method will extract the args from commandline or in run options in IDE
     * @param args String array of user input
     */
    public static void main(String[] args){

        // region readArgs
        double min_c_re = Double.parseDouble(args[0]);
        double min_c_im = Double.parseDouble(args[1]);
        double max_c_re = Double.parseDouble(args[2]);
        double max_c_im = Double.parseDouble(args[3]);

        int max_n = Integer.parseInt(args[4]);
        int x = Integer.parseInt(args[5]);
        int y = Integer.parseInt(args[6]);
        int divisions = Integer.parseInt(args[7]);

        List<String> servers = new ArrayList<>(Arrays.asList(args).subList(8, args.length));
        // endregion

        BufferedImage image = new BufferedImage(x, y, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.createGraphics();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < divisions; i++) {
            int start_y = (y / divisions) * i;
            int end_y = (y / divisions) * (i + 1);
            // Will be set to 0 and full width so pictures get divided in strips
            int start_x = 0;
            int end_x = x;

            MakeSubImage makeSubImage = new MakeSubImage(image, g,i, start_x, start_y, makeSettingsUrl(servers.get(i % (servers.size())), i, start_x, start_y, end_x, end_y, min_c_re, min_c_im, max_c_re, max_c_im, x, y, max_n));

            Thread thread = new Thread(makeSubImage);
            threads.add(thread);
            thread.start();
        }

        // Will wait for all threads to be done
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            ImageIO.write(image, "png", new File("combine.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done");
    }

    private static String makeSettingsUrl(String server, int part, int start_x, int start_y, int end_x, int end_y, double min_c_re, double min_c_im, double max_c_re, double max_c_im, int width, int height, int max_n) {
        return "http://" + server +
                "/fractal/makePart/" +
                part + "/" +
                start_x + "/" +
                start_y + "/" +
                end_x + "/" +
                end_y + "/" +
                // {min_c_re}/{min_c_im}/{max_c_re}/{max_c_im}/
                min_c_re + "/" + min_c_im + "/" + max_c_re + "/" + max_c_im + "/" +
                width + "/" +
                height + "/" +
                max_n;
    }


    /**
     * Helper class to make sub images in new threads. Will call the server with the created url from makeSettingsUrl
     */
    private static class MakeSubImage implements Runnable {

        private BufferedImage image;
        private Graphics g;

        private int part;

        private int start_x;
        private int start_y;

        private String url;

        public MakeSubImage(BufferedImage image, Graphics g,int part, int start_x, int start_y, String url) {
            this.image = image;
            this.g = g;
            this.part = part;
            this.start_x = start_x;
            this.start_y = start_y;
            this.url = url;
        }

        /**
         * Method that's called when starting a new thread.
         * Will make API call via makeMultiPulCalls method and receive Base64 string and decode to byte[] and array to image.
         */
        public void run() {
            byte[] decoded = new byte[0];
            try {
                decoded = Base64.getDecoder().decode(fetchSubImage(url));

            } catch (SocketTimeoutException e) {
                System.err.println("Request timeout, please increase number of divisions");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(decoded != null) {
                InputStream inputStream = new ByteArrayInputStream(decoded);
                try {
                    image = ImageIO.read(inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                g.drawImage(image, start_x, start_y, null);
            }else {
                System.err.println("Return base64 String null");
            }
            System.out.println("Division: " + part+ " done");
        }
    }

    /**
     * Rest Get method to fetch byte64 string from server of the subImage
     * @param url made from makeSettingsUrl with all the info to send to server
     * @return base64 string of the fetch sub image
     * @throws IOException if fetch takes longer then 30 sec a timeout will occur,
     */
    public static String fetchSubImage(String url) throws IOException {

        OkHttpClient client = new OkHttpClient().newBuilder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .build();
        Response response = null;
        response = client.newCall(request).execute();
        ResponseBody responseBody = response.body();

        return responseBody != null ? responseBody.string() : null;
    }

}


