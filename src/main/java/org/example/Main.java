package org.example;

import MVPTree.CmpFunc;
import MVPTree.GetDistance;
import MVPTree.MVPBuilder;
import com.mysql.jdbc.StringUtils;
import entity.MVPDP;
import entity.MVPDataType;

import java.io.FileNotFoundException;
import java.io.Flushable;
import java.io.PrintWriter;
import java.sql.Array;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        // error threshold
        int radius = 2;
        // searching column of file
        int column = 2;
        // limit of row
        int limit = 5000;
        String fp = "./dataset/airport.csv";



        //MVPTree
        MVPBuilder mvpBuilder = new MVPBuilder(fp);
        List<List<String>> data = mvpBuilder.getData(limit);
        List<String> queries = new ArrayList<>();
        HashSet<String> q = new HashSet<>();
        for(List<String> s : data){
            queries.add(s.get(column));
//            q.add(s.get(column));
        }
//        System.out.println("unique:" + q.size());

        Instant start = Instant.now();
        GetDistance getDistance = new GetDistance();
        mvpBuilder.mvpTreebuilder(getDistance,limit,column);
        Duration setup = Duration.between(start, Instant.now());
        System.out.println("MVPT setup Spent: " + setup.toMillis() + " milliseconds");
        List<MVPDP> result = new ArrayList<>();

//        search for all values in the column

        for(String s : queries) {
            MVPDP query = new MVPDP(0, s, MVPDataType.STRING);
            result = mvpBuilder.mvpQuery(query, radius);
        }

//       search for one data to confirm the result
//        MVPDP query = new MVPDP(0, "Haxton Airport", MVPDataType.STRING);
//        result = mvpBuilder.mvpQuery(query, radius);
//        System.out.println("number:" + result.size());
//        for(MVPDP p : result){
//            System.out.println(p.getData());
//        }



        Duration duration = Duration.between(start, Instant.now());
        System.out.println("MVPT Total Spent: " + duration.toMillis() + " milliseconds");



        //Original Method
        start = Instant.now();

//        search for all values in the column
        for(String query : queries) {
            List<String> closeStrings = new ArrayList<>();
            for (List<String> str : mvpBuilder.getData(limit)) {
                float distance = getDistance.compare(query, str.get(column));
                if (distance <= radius) {
                    closeStrings.add(str.get(0));
                }
            }
//            System.out.println("number:" + closeStrings.size());
        }

//       search for one data to confirm the result
//        query = new MVPDP(0, "Haxton Airport", MVPDataType.STRING);
//        HashMap<String, Integer> closeStrings = new HashMap<>();
//            for (List<String> str : data) {
//                int distance = (int)getDistance.compare(query.getData().toString(), str.get(column));
//                if (distance <= radius) {
//                    closeStrings.put(str.get(column), distance);
//                }
//            }
//        System.out.println("number:" + closeStrings.size());


        duration = Duration.between(start, Instant.now());
        System.out.println("Origin Spent: " + duration.toMillis() + " milliseconds");

//        check
//        for(String t : closeStrings.keySet()){
//            System.out.println(t + ":" + closeStrings.get(t));
//        }

//
//        System.out.println("Only found in MVPTree：\n");
//
//        for(MVPDP m : result){
//            if(!closeStrings.contains(m.getData().toString())){
//                System.out.println(m.getData());
//            }
//            else closeStrings.remove(m.getData().toString());
//        }
//
//
//        for(String an : closeStrings){
//            System.out.println(an);
//            System.out.println(getDistance.compare(query, new MVPDP(1,an,MVPDataType.STRING)));
//        }

        
//        writer.flush();

    }
}


