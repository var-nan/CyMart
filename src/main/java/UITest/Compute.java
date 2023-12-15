package UITest;

import java.io.File;

/**
 * @author nandhan, Created on 10/12/23
 */
public class Compute{

    private static void initialize(double[][] array) {

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++)
                array[i][j] = Math.random();
        }
    }

    private static void initializeZeros(double[][] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++)
                array[i][j] = 0.0;
        }
    }

    private static void multiply(double[][] array1, double[][] array2, double[][] result) {
        for (int i = 0; i < array1.length; i++) {
            for (int j = 0; j < array2[0].length; j++) {
                double sum = 0;
                for (int k = 0; k < array1[0].length; k++) {
                    sum += array1[i][k] * array2[k][j];
                }
                result[i][j] = sum;
            }
        }
    }

    private static double det(double[][] array) {
        double sum = 0;
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++)
                sum += array[i][j];
        }
        return sum;
    }

    public static void main(String[] args) {
        int M = 10;
        int N = 10;
        int K = 10;

        double[][] A = new double[M][K];
        double[][] B = new double[K][N];

        double[][] C = new double[M][N];

        System.out.println("Starting matrix initialization");
        initialize(A);
        initialize(B);
        initializeZeros(C);

        System.out.println("Starting multiplication");
        multiply(A,B,C);
        multiply(A,C,C);

        double sum = det(C);

        System.out.println("Det is "+sum);

        System.out.println("Task execution completed.");



    }
}
