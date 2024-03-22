package MVPTree;

import entity.MVPDP;

public class GetDistance implements CmpFunc {
    @Override
    public float compare(MVPDP pointA, MVPDP pointB) {
        if(pointB == null || pointA == null) return -1;
        String word1 = (String) pointA.getData();
        String word2 = (String) pointB.getData();
        int len1 = word1.length();
        int len2 = word2.length();
        int [][] dp = new int[len1 + 1][len2 + 1];
        for(int i = 0; i <= len1; i ++){
            dp[i][0] = i;
        }
        for(int j = 0; j <= len2; j ++){
            dp[0][j] = j;
        }
        for(int i = 1; i <= len1; i ++) {
            for (int j = 1; j <= len2; j++) {
                int d = 1;
                if(word1.charAt(i - 1) == word2.charAt(j - 1)){
                    d = 0;
                }
                dp[i][j] = Math.min(dp[i - 1][j - 1] + d, Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
            }
        }
        return dp[len1][len2];
    }

    public float compare(String pointA, String pointB) {
        int len1 = pointA.length();
        int len2 = pointB.length();
        int [][] dp = new int[len1 + 1][len2 + 1];
        for(int i = 0; i <= len1; i ++){
            dp[i][0] = i;
        }
        for(int j = 0; j <= len2; j ++){
            dp[0][j] = j;
        }
        for(int i = 1; i <= len1; i ++) {
            for (int j = 1; j <= len2; j++) {
                int d = 1;
                if(pointA.charAt(i - 1) == pointB.charAt(j - 1)){
                    d = 0;
                }
                dp[i][j] = Math.min(dp[i - 1][j - 1] + d, Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
            }
        }
        return dp[len1][len2];
    }
}
