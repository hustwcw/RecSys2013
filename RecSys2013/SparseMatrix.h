//
//  SparseMatrix.h
//  RecSys2013
//
//  Created by JTangWang on 13-7-7.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#ifndef RecSys2013_SparseMatrix_h
#define RecSys2013_SparseMatrix_h


typedef struct{
    int i, j;
    float rating;
}Triple;

typedef struct
{
    Triple *data;
    int *rpos;
    int mu, nu, tu; // 矩阵的行数，列数，非零元素的个数
}SparseMatrix;
#endif
