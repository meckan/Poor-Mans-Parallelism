package com.example.poormanparallelismserver.Controller;


import org.apache.logging.log4j.util.Base64Util;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/fractal")
public class FractalsController {

    /**
     * Help rest method to create a reference image of
     * @throws IOException
     */
    @GetMapping("/")
    public void makeTestImage() throws IOException {
        int width = 1920, height = 1080, max = 1000;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int black = 0;
        int[] colors = new int[max];
        for (int i = 0; i < max; i++) {
            colors[i] = Color.HSBtoRGB(i / 256f, 1, i / (i + 8f));
        }

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {

                int iteration = getIteration(col,row,width,height,max);

                if (iteration < max) image.setRGB(col, row, colors[iteration]);
                else image.setRGB(col, row, black);
            }
        }
        ImageIO.write(image, "png", new File("mandelbrot.png"));
    }

    /**
     * Rest method to create a base64 string of part of full image of a Mandelbrot set. Using variabels to create image
     * @param part what part of the full image the part is (for user feedback only)
     * @param start_x x cordinate to start from
     * @param start_y y cordinate to start from
     * @param end_x last x cordniate
     * @param end_y last y cordniate
     * @param min_c_re min value of real numbers
     * @param min_c_im min value of complex numbers
     * @param max_c_re max value of real numbers
     * @param max_c_im max value of complex numbers
     * @param width of the full image that this is part of
     * @param height of the full image that this is part of
     * @param inf_n max number of iternations
     * @return base64 string of the created image part
     */
    @GetMapping("/makePart/{part}/{start_x}/{start_y}/{end_x}/{end_y}/" +
            "{min_c_re}/{min_c_im}/{max_c_re}/{max_c_im}/{width}/{height}/{inf_n}")
    public String makeImagePart(
            @PathVariable int part,
            @PathVariable int start_x, @PathVariable int start_y,
            @PathVariable int end_x, @PathVariable int end_y,

            @PathVariable double min_c_re, @PathVariable double min_c_im,
            @PathVariable double max_c_re, @PathVariable double max_c_im,

            @PathVariable int width, @PathVariable int height,
            @PathVariable int inf_n){

        BufferedImage image = new BufferedImage(end_x - start_x, end_y - start_y, BufferedImage.TYPE_INT_RGB);

        int black = 0;
        int[] colors = new int[inf_n];
        for (int i = 0; i < inf_n; i++) {
            colors[i] = Color.HSBtoRGB(i / 256f, 1, i / (i + 8f));
        }

        for (int row = start_y; row < end_y; row++) {
            for (int col = start_x; col < end_x; col++) {

                int iteration = getIteration(col,row,width,height, inf_n,
                        min_c_re,min_c_im,max_c_re,max_c_im);
                try {
                    if (iteration < inf_n) image.setRGB(col - start_x, row - start_y, colors[iteration]);
                    else image.setRGB(col - start_x, row - start_y, black);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("Col: " + col + " Start: " + start_x + " Col-start: " + (col - start_x));
                    System.err.println("Row: " + row + " Start: " + start_y + " Row-start: " + (row - start_y));
                    System.out.println();
                }
            }
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image,"png",outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Part: " + part + " done in server");
        return Base64Utils.encodeToString(outputStream.toByteArray());
    }

    /**
     * Help method to calculate iterations, using the min and max as i think they should be used.
     * @param col x cordinate
     * @param row y cordinate
     * @param width full image width
     * @param height full images height
     * @param max maximum numbers of iterations
     * @param min_c_re min value of real
     * @param min_c_im min value of complex
     * @param max_c_re max value of real
     * @param max_c_im max value of complex
     * @return iterations made in loop
     */
    private int getIteration(int col, int row, int width,int height, int max,
                             double min_c_re, double min_c_im, double max_c_re, double max_c_im){
        double c_re = (col - width / 2) * 4.0 / width;
        double c_im = (row - height / 2) * 4.0 / height;

        if(c_re < min_c_re)
            c_re = min_c_re;
        else if(c_re > max_c_re)
            c_re = max_c_re;

        if(c_im < min_c_im)
            c_im = min_c_im;
        else if(c_im > max_c_im)
            c_im = max_c_im;

        double x = 0, y = 0;

        int iteration = 0;
        while (x * x + y * y < 4 && iteration < max) {
            double x_new = x * x - y * y + c_re;
            y = 2 * x * y + c_im;
            x = x_new;
            iteration++;
        }
        return iteration;
    }

    /**
     * Help method to calculate iterations, WITHOUT min and max.
     * @param col x cordinate
     * @param row y cordinate
     * @param width full image width
     * @param height full images height
     * @param max maximum numbers of iterations
     * @return iterations made in loop
     */
    private int getIteration(int col, int row, int width,int height, int max){
        double c_re = (col - width / 2) * 4.0 / width;
        double c_im = (row - height / 2) * 4.0 / height;

        double x = 0, y = 0;

        int iteration = 0;
        while (x * x + y * y < 4 && iteration < max) {
            double x_new = x * x - y * y + c_re;
            y = 2 * x * y + c_im;
            x = x_new;
            iteration++;
        }
        return iteration;
    }
}
