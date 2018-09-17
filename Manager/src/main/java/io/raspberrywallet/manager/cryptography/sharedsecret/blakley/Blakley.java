package io.raspberrywallet.manager.cryptography.sharedsecret.blakley;


import io.raspberrywallet.manager.cryptography.sharedsecret.blakley.JLinAlg.*;

import java.math.BigInteger;
import java.util.Random;

/**
 * <p> Calculate Bakley scheme. You need t keys of n for resolve the secret </p>
 * <p> A Blakley's key is an array of BigInteger. It defines a hyperplane</p>
 * <p> Note: <br>
 * blackey class need use library JLinAlg http://jlinalg.sourceforge.net/ </p>
 */
public class Blakley {
    /**
     * Create a key using Blakley's scheme
     * @param cords coordinates of secret (use divide for generate)
     * @param bits size of the key
     * @return Blakley's scheme key
     */
    static public BigInteger[] createdKey(BigInteger cords[], int bits){
        BigInteger equation[] = new BigInteger[cords.length+1];
        BigInteger sum = new BigInteger("0");
        for(int i = 0; i < cords.length; i++){
            equation[i] = new BigInteger(bits, new Random());
            sum = sum.add(cords[i].multiply(equation[i]));
            //System.out.println(cords[i] + " * " + equation[i]);
        }
        equation[cords.length] = sum;
        //System.out.println(sum);
        return equation;
    }

    /**
     * Solve a group of key using Blakley's scheme
     * @param cords array of secret keys (minimal of t keys)
     * @return secret
     */
    static public byte[] solutionKey(BigInteger[][] cords){
        FieldElement fields[][] = new FieldElement[cords.length][cords[0].length-1];

        Vector vector = new Vector(cords.length);
        for(int i1 = 0; i1 < cords.length; i1++){
            for(int i2 = 0; i2 < cords[0].length-1; i2++){
                fields[i1][i2] = new Rational(cords[i1][i2]);
            }
            vector.set(i1+1, new Rational(cords[i1][cords[0].length-1]));
        }

        Matrix matrix = new Matrix(fields);

        //System.out.println("Vector \n" + vector);
        //System.out.println("Matrix \n" + matrix);
        Vector solve = new LinSysSolver().solve(matrix, vector);
        //System.out.println("Solution \n" + solve);
        BigInteger solution[] = new BigInteger[solve.length()];

        for(int i = 1; i < vector.length()+1; i++){
            solution[i-1] = new BigInteger(solve.getEntry(i).toString());
        }

        int size = 0;
        for(int i = 0; i < solution.length; i++){
            size += solution[i].toByteArray().length;
        }

        //System.out.println("size: "+ size);
        byte[] ret = new byte[size];
        int pos = size-1;
        for(int i = 0; i < solution.length; i++){
            for(int j = solution[i].toByteArray().length-1; j >= 0 ; j--){
                ret[pos] = solution[i].toByteArray()[j];
                pos--;
                //System.out.println("pos: "+ pos);
            }
        }
        return ret;
    }

    /**
     * Create coordinates for calculate a key using Blakley's scheme. Divide a secret in t parts
     * @param n numbers of parts
     * @param secret secret
     */
    static public BigInteger[] divide(int n, byte[] secret){
        byte[] scrt = secret;
        BigInteger[] s = new BigInteger[n];
        int size = secret.length/n;
        byte[] aux = new byte[size];
        for(int i = 0; i < n-1; i++){
            for(int j = 0; j < size; j++){
                //System.out.println("i,j :"+i+" , "+j+" -> "+(secret.length-(1+(i*size)+j)));
                aux[size-(j+1)] = secret[secret.length-(1+(i*size)+j)];
                scrt[secret.length-(1+(i*size)+j)] = 0;
            }
            s[i] = new BigInteger(aux);
            //System.out.println("s["+i+"] = "+s[i]);
        }
        aux = new byte[secret.length-(size*(n-1))];
        for(int i=0; i < aux.length; i++)
            aux[i] = scrt[i];

        s[n-1] = new BigInteger(aux);
        return s;
    }
}