//
//  SparseMatrix.h
//  RecSys2013
//
//  Created by JTangWang on 13-7-7.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#ifndef RecSys2013_SparseMatrix_h
#define RecSys2013_SparseMatrix_h

template <class T>
struct Triple{
    int i, j;
    T elem;
	Triple():i(0), j(0), elem(0){}
	Triple(int row, int col, const T &value)
		:i(row), j(col), elem(value)
	{}
};

template <class T>
class SparseMatrix
{
public:
    Triple<T> *data; // 非零元三元组表，以行序或者为主序进行排列
    int *rpos;
    int mu, nu, tu; // 矩阵的行数，列数，非零元素的个数
    
    SparseMatrix(int size, int rowCount, int colCount)
    :mu(rowCount), nu(colCount), tu(size)
    {
        data = new Triple<T>[size];
        rpos = new int[rowCount+1]; // 最后一个表示数组结尾的下一个位置
    }
    
    // 获取行列位置上元素的值，使用二分查找法在一行上查找
    // 按每行10000个元素，非零元素占0.5%计算，共有50个元素需要搜索，二分查找的平均比较6次不到
    T getElem(int row, int col) const
    {
        int start = rpos[row];      // 当前行的起始位置
        int end = rpos[row + 1];    // 当前行的结束位置
        int mid = (start+end)/2;
        while (col != data[mid].j && start < end) {
            if (col > data[mid].j)
            {
                start = mid + 1;
            }
            else
            {
                end = mid;
            }
            mid = (start + end)/2;
        }
        
        return (data[mid].j == col) ? data[mid].elem : T(0);
    }
    
    void transposeMatrix(SparseMatrix &otherMatrix) const
    {
        int q = 0;
        for (int col = 0; col < this->nu; ++col) {
            otherMatrix.rpos[col] = q;
            for (int p = 0; p < this->tu; ++p) {
                if (this->data[p].j == col) {
                    otherMatrix.data[q].i = this->data[p].j;
                    otherMatrix.data[q].j = this->data[p].i;
                    otherMatrix.data[q].elem = this->data[p].elem;
                    ++q;
                }
            }
        }
        otherMatrix.rpos[this->nu] = q;
    }
    
    ~SparseMatrix()
    {
        delete[] data;
        delete[] rpos;
    }
};
#endif
