package me.blutkrone.rpgcore.resourcepack.utils; /**
 * * Java Program to Implement Bresenham Line Algorithm
 **/


import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


/**
 * Class Bresenham
 **/
public class Bresenham {

    /**
     * Function main()
     **/

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);

        System.out.println("Bresenham Line Algorithm");
        System.out.println("\nEnter dimensions of grid");

        int rows = scan.nextInt();
        int cols = scan.nextInt();

        Point[][] grid = new Point[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                grid[i][j] = new Point(i, j);

        System.out.println("\nEnter coordinates of point 1 and point 2");
        int sr = scan.nextInt();
        int sc = scan.nextInt();
        int fr = scan.nextInt();
        int fc = scan.nextInt();

        Bresenham b = new Bresenham();
        List<Point> line = b.findLine(grid, sr, sc, fr, fc);
        b.plot(grid, line);
    }

    /**
     * function findLine() - to find that belong to line connecting the two points
     **/
    public List<Point> findLine(Point[][] grid, int x0, int y0, int x1, int y1) {
        List<Point> line = new ArrayList<Point>();
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int e2;

        while (true) {
            line.add(grid[x0][y0]);
            if (x0 == x1 && y0 == y1)
                break;
            e2 = 2 * err;
            if (e2 > -dy) {
                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx) {
                err = err + dx;
                y0 = y0 + sy;
            }
        }

        return line;
    }

    /**
     * function plot() - to visualize grid
     **/

    public void plot(Point[][] grid, List<Point> line) {
        int rows = grid.length;
        int cols = grid[0].length;

        System.out.println("\nPlot : \n");

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (line.contains(grid[i][j]))
                    System.out.print("*");
                else
                    System.out.print("X");
            }

            System.out.println();
        }
    }
}