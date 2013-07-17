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

struct SparseMatrix
{
    Triple *data; // 非零元三元组表，以行序或者为主序进行排列
    int *rpos;
    int mu, nu, tu; // 矩阵的行数，列数，非零元素的个数
    
    SparseMatrix(int size, int rowCount)
    {
        data = new Triple[size];
        rpos = new int[rowCount+1]; // 最后一个表示数组结尾的下一个位置
    }
    
    ~SparseMatrix()
    {
        delete[] data;
        delete[] rpos;
    }
};
#endif
