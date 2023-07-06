package entity;

public enum MVPError {
    MVP_SUCCESS,            /* no error */
    MVP_ARGERR,             /* argument error */
    MVP_NODISTANCEFUNC,     /* no distance function found */
    MVP_MEMALLOC,           /* mem alloc error */
    MVP_NOLEAF,             /* could not alloc leaf node */
    MVP_NOINTERNAL,         /* could not alloc internal node */
    MVP_PATHALLOC,          /* path alloc error */
    MVP_VPNOSELECT,         /* could not select vantage points */
    MVP_NOSV1RANGE,         /* could not calculate range of points from sv1 */
    MVP_NOSV2RANGE,         /* could not calculate range of points from sv2 */
    MVP_NOSPACE,            /* points too close to one another, too compact */
    MVP_NOSORT,             /* unable to sort points */
    MVP_FILEOPEN,           /* trouble opening file */
    MVP_FILECLOSE,          /* trouble closing file */
    MVP_MEMMAP,             /* mem map trouble */
    MVP_MUNMAP,             /* mem unmap trouble */
    MVP_NOWRITE,            /* could not write to file */
    MVP_FILETRUNCATE,       /* could not extend file */
    MVP_MREMAPFAIL,         /* unable to map/unmap file */
    MVP_TYPEMISMATCH,       /* trying to add datapoints of one datatype */
    /* to tree that already contains another type */
    MVP_KNEARESTCAP,        /* number results found reaches knearest limit */
    MVP_EMPTYTREE,
    MVP_NOSPLITS,           /* unable to calculate split points */
    MVP_BADDISTVAL,         /* val from distance function either NaN or less than 0 */
    MVP_FILENOTFOUND,       /* file not found */
    MVP_UNRECOGNIZED;       /* unrecognized node */
}
