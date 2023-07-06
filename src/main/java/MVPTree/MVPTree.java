package MVPTree;

import entity.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.List;

import static java.lang.Float.isNaN;

public class MVPTree {
    private int branchFactor;
    private int pathLength;
    private int leafCapacity;
    private int fd;
    private int k;
    private MVPDataType dataType;
    private long pos;
    private long size;
    private long pageSize;
    private char[] buf;
    private Node node;
    private static CmpFunc distance;
    private int nbresults;


    public static final int HEADER_SIZE = 32;
    public static final int _FILE_OFFSET_BITS = 32;
    public static final int _LARGEFILE64_SOURCE = 0;
    public static final String TAG = "phashmvp2010";
    public static final int VERSION = 0x01000000;


    // Constructor
    public MVPTree(int branchFactor, int pathLength, int leafCapacity, int fd, int k, MVPDataType dataType,
                   long pos, long size, long pageSize, char[] buf, Node node, CmpFunc distance) {
        this.branchFactor = branchFactor;
        this.pathLength = pathLength;
        this.leafCapacity = leafCapacity;
        this.fd = fd;
        this.k = k;
        this.dataType = dataType;
        this.pos = pos;
        this.size = size;
        this.pageSize = pageSize;
        this.buf = buf;
        this.node = node;
        this.distance = distance;
    }

    public MVPDP dpAlloc(MVPDataType type) {
        MVPDP newdp = new MVPDP();
        newdp.setType(type);
        return newdp;
    }

    //create leaf node
    private static LeafNode createLeaf(int leafcap) {
        List<MVPDP> list = new ArrayList<>(leafcap);
        List<Float> d1 = new ArrayList<>(leafcap);
        List<Float> d2 = new ArrayList<>(leafcap);
        return new LeafNode(NodeType.LEAF,null,null, list, d1, d2,0);
    }
    //create internal node
    private static InternalNode createInternal(int bf){
        List<Float> m1 = new ArrayList<>(bf - 1);
        List<Float> m2 = new ArrayList<>(bf);
        List<Object> child = new ArrayList<>(bf * bf);
        return new InternalNode(NodeType.INTERNAL, null,null, m1, m2, child);
    }
    //find the advantage points
    private static List<Integer> selectVantagePoints(List<MVPDP> points, int nb) {

        int sv1_pos = (nb >= 1) ? 0 : -1;
        int sv2_pos = -1;

        float max_dist = 0.0f;
        float d;
        int i, j;
        for (i = 0; i < nb; i++) {
            for (j = i + 1; j < nb; j++) {
                d = distance.compare(points.get(i), points.get(j));
                if (isNaN(d) || d < 0.0f) {
                    return null;
                }

                if (d > max_dist) {
                    max_dist = d;
                    sv1_pos = i;
                    sv2_pos = j;
                }
            }
        }

        return new ArrayList<>(Arrays.asList(sv1_pos, sv2_pos));
    }

    private static int findSplits(List<MVPDP> points, int nb, MVPDP vp, List<Float> M, int lengthM) {
        if (points == null || nb == 0 || M == null || lengthM == 0) {
            return -1;
        }

        float[] dist = new float[nb];

        for (int i = 0; i < nb; i++) {
            dist[i] = distance.compare(points.get(i), vp);
            if (isNaN(dist[i]) || dist[i] < 0.0f) {
                return -2;
            }
        }

        int min_pos;
        for (int i = 0; i < nb - 1; i++) {
            min_pos = i;
            for (int j = i + 1; j < nb; j++) {
                if (dist[j] < dist[min_pos]) {
                    min_pos = j;
                }
            }

            if (min_pos != i) {
                float tmp = dist[min_pos];
                dist[min_pos] = dist[i];
                dist[i] = tmp;
            }
        }

        for (int i = 0; i < lengthM; i++) {
            int index = (i + 1) * nb / (lengthM + 1);
            if (index <= 0) {
                index = 0;
            }
            if (index >= nb) {
                index = nb - 1;
            }
            M.set(i,dist[index]);
        }

        return 0;
    }

    /* Sort points into bins by distance(points[i], dp) for each i in list, skipping
     * points[sv1_pos] and points[sv2_pos]. Use pivot[LengthM1] array as pivot points
     * to determine which bins. */
    public List<List<MVPDP>> sortPoints(List<MVPDP> points, int nbpoints, int sv1_pos, int sv2_pos, MVPDP vp, List<Integer> counts, List<Float> pivots) {
        if (points == null || vp == null || counts == null || pivots == null || nbpoints == 0) {
            return null;
        }

        int bf = this.getbranchFactor();
        int lengthM1 = bf - 1;

        List<List<MVPDP>> bins = new ArrayList<>(bf);

        counts = new ArrayList<>(bf);

        for (int i = 0; i < bf; i++) {
            List<MVPDP> bin = new ArrayList<>(nbpoints);
            bins.set(i, bin) ;
            if (bins.get(i) == null) {
                return null;
            }
        }

        for (int i = 0; i < nbpoints; i++) {
            if (i == sv1_pos || i == sv2_pos) {
                continue;
            }

            float d = distance.compare(vp, points.get(i));
            if (isNaN(d) || d < 0.0f) {
                return null;
            }

            for (int k = 0; k < lengthM1; k++) {
                if (d <= pivots.get(k)) {
                    List<MVPDP> bin = bins.get(k);
                    bin.set(counts.get(k), points.get(i));
                    bins.set(k, bin);
                    counts.set(k,  counts.get(k) + 1);
                    break;
                }
            }

            if (d > pivots.get(lengthM1 - 1)) {
                List<MVPDP> bin = bins.get(lengthM1);
                bin.set(counts.get(lengthM1), points.get(i));
                bins.set(lengthM1, bin);
                counts.set(lengthM1, counts.get(lengthM1) + 1);
            }
        }

        return bins;
    }

    /* Calculate distances for all points from given vantage point, vp, and
   assign that distance into each points path using the lvl parameter. */
    public int findDistanceRangeForVP(List<MVPDP> points, int nbPoints, MVPDP vp, int lvl) {
        if (points == null || nbPoints == 0 || vp == null ) {
            return -1;
        }

        int error = 0;
        for (int i = 0; i < nbPoints; i++) {
            float d = distance.compare(vp, points.get(i));
            if (Float.isNaN(d) || d < 0.0f) {
                return -2;
            }
            if (lvl < this.pathLength) {
                points.get(i).setPath(lvl, d);
            }
        }

        return error;
    }

    public Node addNode(Node node, List<MVPDP> points, int nbpoints, MVPError error, int lvl){
        Node newnode = node;
        if (nbpoints == 0) { return newnode; }
        if (lvl < 0 || points == null) {
        error = MVPError.MVP_ARGERR;
            return null;
        }
        int bf = this.branchFactor;
        int lengthM1 = bf - 1;
        if(newnode == null){
            int sv1_pos;
            int sv2_pos;
            if(nbpoints <= this.leafCapacity + 2){
                newnode = createLeaf(this.leafCapacity);
                if(newnode == null){
                    error = MVPError.MVP_NOLEAF;
                    return null;
                }

                List<Integer> sv = new ArrayList<>(2);
                if ((sv = selectVantagePoints(points, nbpoints)) == null) {
                    error = MVPError.MVP_VPNOSELECT;
                    return null;
                }
                sv1_pos = sv.get(0);
                sv2_pos = sv.get(1);
                ((LeafNode) newnode).setSv1((sv1_pos >= 0) ? points.get(sv1_pos) : null);
                ((LeafNode) newnode).setSv2((sv2_pos >= 0) ? points.get(sv2_pos) : null);

                if (findDistanceRangeForVP(points, nbpoints, ((LeafNode) newnode).getSv1(), lvl) < 0) {
                    error = MVPError.MVP_NOSV1RANGE;
                    return null;
                }

                if (((LeafNode) newnode).getSv2() != null) {
                    if (findDistanceRangeForVP(points, nbpoints, ((LeafNode) newnode).getSv2(), lvl + 1) < 0) {
                        error = MVPError.MVP_NOSV2RANGE;
                        return null;
                    }
                }
                int count = 0;
                for (int i = 0; i < nbpoints; i++) {
                    if (i == sv1_pos || i == sv2_pos) {
                        continue;
                    }
                    ((LeafNode) newnode).getD1().set(count, distance.compare(points.get(i), ((LeafNode) newnode).getSv1()));
                    ((LeafNode) newnode).getD2().set(count, distance.compare(points.get(i), ((LeafNode) newnode).getSv2()));
                    ((LeafNode) newnode).getPoints().set(count++, points.get(i));
                }
                ((LeafNode) newnode).setNbPoints(count);
            }
            else {
                newnode = createInternal(this.getBranchFactor());
                if(newnode == null){
                    error = MVPError.MVP_NOINTERNAL;
                    return null;
                }
                List<Integer> pos = selectVantagePoints(points, nbpoints);
                if(pos == null){
                    error = MVPError.MVP_VPNOSELECT;
                    return null;
                }
                ((InternalNode) newnode).setSv1(points.get(pos.get(0)));
                ((InternalNode) newnode).setSv2(points.get(pos.get(1)));

                if(findDistanceRangeForVP(points, nbpoints,((InternalNode) newnode).getSv1(),lvl) < 0){
                    error = MVPError.MVP_NOSV1RANGE;
                    return null;
                }

                if(findSplits(points, nbpoints, ((InternalNode) newnode).getSv1(),((InternalNode) newnode).getM1(),lengthM1) < 0){
                    error = MVPError.MVP_NOSPLITS;
                    return null;
                }

                int i,j;
                List<Integer> binlengths = new ArrayList<>();
                List<List<MVPDP>> bins = sortPoints(points, nbpoints, pos.get(0), pos.get(1),((InternalNode) newnode).getSv1(),binlengths,((InternalNode) newnode).getM1());
                if(bins == null){
                    error = MVPError.MVP_NOSORT;
                    return null;
                }
                for(i = 0; i < branchFactor; i++){
                    if(findDistanceRangeForVP(bins.get(i), binlengths.get(i), ((InternalNode) newnode).getSv2(),lvl + 1) < 0){
                        error = MVPError.MVP_NOSV2RANGE;
                        return null;
                    }
                    if(findSplits(bins.get(i), binlengths.get(i), ((InternalNode) newnode).getSv2(),((InternalNode) newnode).getM2().subList(i * lengthM1,((InternalNode) newnode).getM2().size()), lengthM1) < 0){
                        error = MVPError.MVP_NOSPLITS;
                        return null;
                    }

                    List<Integer> bin2lengths = new ArrayList<>();
                    List<List<MVPDP>> bins2 = sortPoints(bins.get(i), bin2lengths.get(i),-1, -1, ((InternalNode) newnode).getSv2(),bin2lengths,((InternalNode) newnode).getM2().subList(i * lengthM1,((InternalNode) newnode).getM2().size()));

                    for(j = 0; j < branchFactor; j++){
                        Node child = addNode(null, bins2.get(j), bin2lengths.get(j), error, lvl = 2);
                        ((InternalNode) newnode).getChildNodes().set(i * branchFactor + j,child);
                    }
                }
            }
        }
        else {
            if(newnode.getType() == NodeType.LEAF){
                if(((LeafNode) newnode).getNbPoints() + nbpoints <= leafCapacity){
                    if(findDistanceRangeForVP(points, nbpoints, ((LeafNode) newnode).getSv1(),lvl)< 0){
                        error = MVPError.MVP_NOSV1RANGE;
                        return newnode;
                    }

                    int pos = 0;
                    if(((LeafNode) newnode).getSv2() == null){
                        ((LeafNode) newnode).setSv2(points.get(0));
                        pos = 1;
                    }
                    if(findDistanceRangeForVP(points, nbpoints,((LeafNode) newnode).getSv2(), lvl + 1) < 0){
                        error = MVPError.MVP_NOSV2RANGE;
                        return newnode;
                    }

                    int count = ((LeafNode) newnode).getNbPoints();
                    for(; pos < nbpoints; pos++){
                        ((LeafNode) newnode).getD1().set(count, distance.compare(points.get(pos), ((LeafNode) newnode).getSv1()));
                        ((LeafNode) newnode).getD2().set(count, distance.compare(points.get(pos), ((LeafNode) newnode).getSv2()));
                        ((LeafNode) newnode).getPoints().set(count++, points.get(pos));
                    }
                    ((LeafNode) newnode).setNbPoints(count);
                }
                else{
                    int newnb = ((LeafNode) newnode).getNbPoints() + nbpoints;
                    if(((LeafNode) newnode).getSv1() != null) newnb++;
                    if(((LeafNode) newnode).getSv2() != null) newnb++;
                    List<MVPDP> tmppts = new ArrayList<>(newnb);

                    int i, index = 0;
                    if(((LeafNode) newnode).getSv1() != null){
                        tmppts.set(index++, ((LeafNode) newnode).getSv1());
                    }
                    if(((LeafNode) newnode).getSv2() != null){
                        tmppts.set(index++, ((LeafNode) newnode).getSv2());
                    }
                    for(i = 0; i < ((LeafNode) newnode).getNbPoints(); i++){
                        tmppts.set(index++, ((LeafNode) newnode).getPoints().get(i));
                    }
                    for(i = 0; i < nbpoints; i++){
                        tmppts.set(index++, points.get(i));
                    }
                    newnode = addNode(null, tmppts, newnb, error,lvl);
                }
            }
            else{
                if(findDistanceRangeForVP(points, nbpoints, ((InternalNode) newnode).getSv1(),lvl) < 0){
                    error = MVPError.MVP_NOSV1RANGE;
                    return newnode;
                }

                List<Integer> binlengths = null;
                List<List<MVPDP>> bins = sortPoints(points, nbpoints, -1, -1, ((InternalNode) newnode).getSv1(),binlengths,((InternalNode) newnode).getM1());
                for(int i = 0; i < branchFactor; i++){
                    if(binlengths.get(i) <= 0){
                        continue;
                    }
                    int j;
                    if(findDistanceRangeForVP(bins.get(i), binlengths.get(i),((InternalNode) newnode).getSv2(),lvl + 1)< 0){
                        error = MVPError.MVP_NOSV2RANGE;
                        return newnode;
                    }

                    List<Integer> bin2lengths = new ArrayList<>();
                    List<List<MVPDP>> bins2 = sortPoints(bins.get(i), binlengths.get(i), -1, -1, ((InternalNode) newnode).getSv2(),bin2lengths, ((InternalNode) newnode).getM2().subList(i * lengthM1, ((InternalNode) newnode).getM2().size() ));
                    for(j = 0; j < branchFactor; j++){
                        Node child;
                        child = addNode((Node) ((InternalNode) newnode).getChildNodes().get(i * branchFactor + j),bins2.get(j),bin2lengths.get(j),error, lvl + 2);
                        ((InternalNode) newnode).getChildNodes().set(i * branchFactor + j, child);
                        if(error != MVPError.MVP_SUCCESS)break;
                    }
                }
            }
        }
        return newnode;
    }


    MVPError mvpAdd(List<MVPDP> points, int nbpoints){
        MVPError err = MVPError.MVP_SUCCESS;
        if(nbpoints == 0)return err;
        if(points != null){
            if(dataType == null){
                dataType = points.get(0).getType();
            }
            for(int i = 0; i < nbpoints; i++){
                points.get(i).setPath(new ArrayList<>(pathLength));
            }
            node = addNode(node,points,nbpoints,err,0);
        }
        else{
            err = MVPError.MVP_ARGERR;
        }
       return err;
    }

    MVPError retrieve(Node node, MVPDP target, float radius, List<MVPDP>results, int lvl){
        MVPError err = MVPError.MVP_SUCCESS;
        int bf = branchFactor;
        int lengthM1 = bf - 1;
        float d1,d2;
        if(node == null) return err;

        if(node.getType() == NodeType.LEAF){
            d1 = distance.compare(target, ((LeafNode)node).getSv1());
            if(isNaN(d1) || d1 < 0.0f){
                return MVPError.MVP_BADDISTVAL;
            }

            if(lvl < pathLength)target.setPath(lvl, d1);
            if(d1 <= radius){
                results.add(nbresults, ((LeafNode) node).getSv1());
                if(nbresults >= k)return MVPError.MVP_KNEARESTCAP;
            }
            if(((LeafNode) node).getSv2() != null){
                d2 = distance.compare(target, ((LeafNode) node).getSv2());

                if(isNaN(d2) || d2 < 0.0f){
                    return MVPError.MVP_BADDISTVAL;
                }
                if(d2 <= radius){
                    results.add(nbresults, ((LeafNode) node).getSv2());
                    nbresults++;
                    if(nbresults >= k)return MVPError.MVP_KNEARESTCAP;
                }
                if(lvl + 1 < pathLength){
                    target.setPath(lvl + 1, d2);
                }
                for(int i = 0; i < ((LeafNode) node).getNbPoints(); i++){
                    if(d1 - radius <= ((LeafNode) node).getD1().get(i) && d1 + radius >= ((LeafNode) node).getD1().get(i)) {
                        if (d2 - radius <= ((LeafNode) node).getD2().get(i) && d2 + radius >= ((LeafNode) node).getD2().get(i)) {
                            int endpath = (lvl + 1 < pathLength) ? lvl + 1 : pathLength;
                            int skip = 0;
                            for(int j = 0; j < endpath; j++){
                                if(target.getPath().get(j) - radius <= ((LeafNode) node).getPoints().get(i).getPath().get(j)
                                && target.getPath().get(j) + radius >= ((LeafNode) node).getPoints().get(i).getPath().get(j))
                                    continue;
                                else {
                                    skip = 1;
                                    break;
                                }
                            }
                            if(skip == 0){
                                float d = distance.compare(target, ((LeafNode) node).getPoints().get(i));
                                if(isNaN(d) || d < 0.0f){
                                    return MVPError.MVP_BADDISTVAL;
                                }
                                if(d <= radius){
                                    results.add(nbresults, ((LeafNode) node).getPoints().get(i));
                                    nbresults++;
                                    if(nbresults >= k)return MVPError.MVP_KNEARESTCAP;
                                }
                            }
                        }
                    }
                }
            }
        }
        else if(node.getType() == NodeType.INTERNAL){
            d1 = distance.compare(target, ((InternalNode) node).getSv1());
            if(isNaN(d1) || d1 < 0.0f){
                return MVPError.MVP_BADDISTVAL;
            }
            if(d1 <= radius){
                results.add(nbresults, ((InternalNode) node).getSv1());
                nbresults++;
                if(nbresults >= k) return MVPError.MVP_KNEARESTCAP;
            }
            if(lvl < pathLength) target.setPath(lvl,d1);
            d2 = distance.compare(target, ((InternalNode) node).getSv2());
            if(isNaN(d2) || d2 < 0.0f){
                return MVPError.MVP_BADDISTVAL;
            }
            if(d2 <= radius){
                results.add(nbresults, ((InternalNode) node).getSv2());
                nbresults++;
                if(nbresults >= k) return MVPError.MVP_KNEARESTCAP;
            }
            if(lvl + 1 < pathLength)target.setPath(lvl + 1, d2);
            /* check <= each 1st level bins */
            for(int i = 0; i < lengthM1; i++){
                if(d1 - radius <= ((InternalNode) node).getM1().get(i)){
                    for(int j = 0; j < lengthM1; j++){
                        if(d2 - radius <= ((InternalNode) node).getM2().get(i * lengthM1 + j)){
                            err = retrieve((Node) ((InternalNode) node).getChildNodes().get(i * bf + j),target,radius,results,lvl + 2);
                            if(err != MVPError.MVP_SUCCESS)return err;
                        }
                    }
                    /* check >= last 2nd level bin  */
                    if(d2 + radius >= ((InternalNode) node).getM2().get(i * lengthM1 + lengthM1 - 1)){
                        err = retrieve((Node) ((InternalNode) node).getChildNodes().get(i * bf + lengthM1),target,radius,results,lvl + 2)
                    }
                }
            }
            /* check >= last 1st level bin */
            if(d1 + radius >= ((InternalNode) node).getM1().get(lengthM1 - 1)){
                /* check <= each 2nd level bins */
                for(int j = 0; j < lengthM1; j++){
                    if(d2 - radius <= ((InternalNode) node).getM2().get(lengthM1 + lengthM1 + j)){
                        err = retrieve((Node) ((InternalNode) node).getChildNodes().get(bf * lengthM1 + j),target,radius,results,lvl + 2);
                        if(err != MVPError.MVP_SUCCESS)return err;
                    }
                }
                if (d2 + radius >= ((InternalNode) node).getM2().get(lengthM1 * lengthM1 + lengthM1 - 1)) {
                    err = retrieve((Node) ((InternalNode) node).getChildNodes().get(bf * lengthM1 + lengthM1),target,radius,results,lvl + 2);
                    if(err != MVPError.MVP_SUCCESS)return err;
                }
            }
            else err = MVPError.MVP_UNRECOGNIZED;
        }
        return err;
    }

    List<MVPDP> mvpRetrieve(MVPDP target, float radius, MVPError err){
        nbresults = 0;
        err = MVPError.MVP_SUCCESS;
        if(node == null){
            err = MVPError.MVP_EMPTYTREE;
            return null;
        }
        List<MVPDP> results = new ArrayList<>();
        target.setPath(new ArrayList<>(pathLength));
        k = 100000;
        err = retrieve(node,target,radius,results,0);
        return results;
    }





    private int getbranchFactor() {
        return branchFactor;
    }


    public int getBranchFactor() {
        return branchFactor;
    }

    public void setBranchFactor(int branchFactor) {
        this.branchFactor = branchFactor;
    }

    public int getPathLength() {
        return pathLength;
    }

    public void setPathLength(int pathLength) {
        this.pathLength = pathLength;
    }

    public int getLeafCapacity() {
        return leafCapacity;
    }

    public void setLeafCapacity(int leafCapacity) {
        this.leafCapacity = leafCapacity;
    }

    public int getFd() {
        return fd;
    }

    public void setFd(int fd) {
        this.fd = fd;
    }

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public MVPDataType getDataType() {
        return dataType;
    }

    public void setDataType(MVPDataType dataType) {
        this.dataType = dataType;
    }

    public long getPos() {
        return pos;
    }

    public void setPos(long pos) {
        this.pos = pos;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize;
    }

    public char[] getBuf() {
        return buf;
    }

    public void setBuf(char[] buf) {
        this.buf = buf;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public CmpFunc getDist() {
        return distance;
    }

    public void setDist(CmpFunc dist) {
        this.distance = dist;
    }
}



