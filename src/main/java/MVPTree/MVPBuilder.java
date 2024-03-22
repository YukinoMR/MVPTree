package MVPTree;

import entity.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;


public class MVPBuilder {
    private MVPTree mvpTree;
    int dataLength = 0;
    int bf = 2;
    int p = 20;
    int k = 25;

    private static String fp = "./dataset/airport.csv";

    public MVPBuilder(String fp){
        MVPBuilder.fp = fp;
    }
    public void mvpTreebuilder(CmpFunc distance, int limit, int column) {
        if (distance == null) {
            return;
        }
        mvpTree = new MVPTree(bf,p,k,0,0, MVPDataType.STRING,0,0,getSystemPageSize(),null,null,distance);
        MVPError err= mvpTree.mvpAdd(pointsGenerate(limit, column),dataLength);
        System.out.println(err);
    }

    public List<MVPDP> mvpQuery(MVPDP data,float radius){
        MVPError error = MVPError.MVP_SUCCESS;
        List<MVPDP> results = new ArrayList<>();
        results = mvpTree.mvpRetrieve(data,radius,error);
        return results;
    }

    public List<MVPDP> pointsGenerate(int limit, int column){
        List<List<String>> data = getData(limit);
        List<MVPDP> points = new ArrayList<>();
        for(int i = 0; i < data.size(); i++){
            points.add(new MVPDP(i, data.get(i).get(column),MVPDataType.STRING));
        }
        dataLength = points.size();
        return  points;
    }
    public List<List<String>> getData(int limit) {
        List<List<String>> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fp))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                List<String> row = new ArrayList<>(Arrays.asList(columns));
                data.add(row);
                if(data.size() >= limit)break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public MVPError _mvptree_print(PrintWriter stream, Node node, int lvl) {
        MVPError error = MVPError.MVP_SUCCESS;
        int bf = mvpTree.getBranchFactor(), lengthM1 = bf-1, fanout = bf*bf;

        if (node != null) {
            if (node.getType() == NodeType.LEAF) {
                stream.printf("LEAF%d  (%d points)\n", lvl, ((LeafNode) node).getNbPoints());
                if (((LeafNode) node).getSv1() != null) {
                    stream.printf("    sv1: %s\n", ((LeafNode) node).getSv1().getId());
                }
                if (((LeafNode) node).getSv2()  != null) {
                    stream.printf("    sv2: %s\n", ((LeafNode) node).getSv1().getId());
                }

                for (int i = 0; i < ((LeafNode) node).getNbPoints(); i++) {
                    stream.printf("        point[%d]: %s\n", i, ((LeafNode) node).getPoints().get(i).getId());
                }
            } else if (node.getType() == NodeType.INTERNAL) {
                stream.printf("INTERNAL%d\n", lvl);
//                System.out.printf("INTERNAL%d\n", lvl);
                stream.printf("  sv1: %s\n", ((InternalNode) node).getSv1().getId());
                stream.printf("  sv2: %s\n", ((InternalNode) node).getSv2().getId());

                for (int i = 0; i < lengthM1; i++) {
                    stream.printf("  M1[%d] = %.4f;", i, ((InternalNode) node).getM1().get(i));
                }
                for (int i = 0; i < bf; i++) {
                    stream.printf("  M2[%d] = %.4f;", i, ((InternalNode) node).getM2().get(i));
                }
                stream.println();

                for (int i = 0; i <  ((InternalNode) node).getChildNodes().size(); i++) {
                    error = _mvptree_print(stream, (Node) ((InternalNode) node).getChildNodes().get(i), lvl+2);
                    if (error != MVPError.MVP_SUCCESS) {
                        break;
                    }
                }
            } else {
                error = MVPError.MVP_UNRECOGNIZED;
            }

        } else {
            stream.printf("NULL%d\n", lvl);
        }

        return error;
    }


    public MVPError mvptree_print(PrintWriter stream) {
        if (stream == null || mvpTree == null) {
            return MVPError.MVP_ARGERR;
        }

        MVPError err = _mvptree_print(stream, mvpTree.getNode(), 0);
        if (err != MVPError.MVP_SUCCESS) {
            stream.printf("malformed tree: %s\n", err);
        }

        return err;
    }



    private long getSystemPageSize() {
        try {
            FileStore fileStore = Files.getFileStore(Path.of(""));
            return fileStore.getBlockSize();
        } catch (IOException e) {
            e.printStackTrace();
            // Return a default page size if unable to determine the system page size
            return 4096; // 4KB as an example
        }
    }
}
