package com.gcj.test;

public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String s1 = "\\\\=\\jetbrains";
        String s2 = "\\' = '";
        String s3 = "\\\\\\' = \\'";
        String s = "Levi's Boys 8-20 Levi'S Fabric Web Belt, One Size Fits Most,Black,One Size";
        s = s.replaceAll("\'", "\\\\\\\'");
        System.out.println(s1);

        System.out.println(s2);
        System.out.println(s3);
        System.out.println(s);
    }
}
