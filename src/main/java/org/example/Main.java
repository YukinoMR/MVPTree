package org.example;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>();
        list.add(2);
        list.add(6);
        list.add(8);
        List<List<Integer>> l = new ArrayList<>();
        l.add(list);
        l.get(0).set(0,7);
        System.out.println(l);
    }
}s