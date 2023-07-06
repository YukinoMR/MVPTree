package MVPTree;

import entity.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;

public class MVPBuilder {
    MVPTree mvpTree;
    public void mvpTreebuilder(CmpFunc distance, int bf, int p, int k) {
        if (distance == null) {
            return;
        }
        mvpTree = new MVPTree(bf,p,k,0,0, MVPDataType.STRING,0,0,getSystemPageSize(),null,null,distance);
    }

    public MVPError _mvptree_print(PrintWriter stream, MVPTree tree, Node node, int lvl) {
        MVPError error = MVPError.MVP_SUCCESS;
        Node next_node = node;
        int bf = tree.getBranchFactor(), lengthM1 = bf-1, lengthM2 = bf, fanout = bf*bf;

        if (next_node != null) {
            if (next_node.getType() == NodeType.LEAF) {
                stream.printf("LEAF%d  (%d points)\n", lvl, ((LeafNode)next_node).getNbPoints());
                if (((LeafNode)next_node).getSv1() != null) {
                    stream.printf("    sv1: %s\n", ((LeafNode)next_node).getSv1().getId());
                }
                if (((LeafNode)next_node).getSv2()  != null) {
                    stream.printf("    sv2: %s\n", ((LeafNode)next_node).getSv1().getId());
                }

                for (int i = 0; i < ((LeafNode)next_node).getNbPoints(); i++) {
                    stream.printf("        point[%d]: %s\n", i, ((LeafNode) next_node).getPoints().get(i).getId());
                }
            } else if (next_node.getType() == NodeType.INTERNAL) {
                stream.printf("INTERNAL%d\n", lvl);
                stream.printf("  sv1: %s\n", ((InternalNode)next_node).getSv1().getId());
                stream.printf("  sv2: %s\n", ((InternalNode)next_node).getSv2().getId());

                for (int i = 0; i < lengthM1; i++) {
                    stream.printf("  M1[%d] = %.4f;", i, ((InternalNode) next_node).getM1().get(i));
                }
                for (int i = 0; i < lengthM2; i++) {
                    stream.printf("  M2[%d] = %.4f;", i, ((InternalNode) next_node).getM2().get(i));
                }
                stream.println();

                for (int i = 0; i < fanout; i++) {
                    error = _mvptree_print(stream, tree, (Node) ((InternalNode) next_node).getChildNodes().get(i), lvl+2);
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


    public MVPError mvptree_print(PrintWriter stream, MVPTree tree) {
        if (stream == null || tree == null) {
            return MVPError.MVP_ARGERR;
        }

        MVPError err = _mvptree_print(stream, tree, tree.getNode(), 0);
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
