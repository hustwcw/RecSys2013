//
//  BasicPMF.cpp
//  RecSys2013
//
//  Created by JTangWang on 13-7-7.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#include "BasicPMF.h"
#include "SparseMatrix.h"

#include <cmath>



using namespace std;


BasicPMF::BasicPMF(int uCount, int bCount, int f)
:userCount(uCount), businessCount(bCount), factor(f)
{
    srand((unsigned int)clock());
    matrixP = new float[factor][UserSize];
    matrixQ = new float[factor][BusinessSize];
    for (int i = 0; i < factor; ++i) {
        for (int j = 0; j < UserSize; ++j) {
            matrixP[i][j] = sqrt(((rand()%5)+1.0)/(float)factor);
        }
    }
    for (int i = 0; i < factor; ++i) {
        for (int j = 0; j < BusinessSize; ++j) {
            matrixQ[i][j] = sqrt(((rand()%5)+1.0)/(float)factor);
        }
    }
}


void BasicPMF::compute(const SparseMatrix<float> &starMatrix, const SparseMatrix<float> &transposeStarMatrix)
{
    double midRMSE = 10.0f;
    
    int count = 0;
    while((midRMSE > iterThreshold) && (count < maxIterNum))   //只要其中一个条件不满足就停止
    {
        float *term1 = new float[factor];
        float *term2 = new float[factor];
        // 计算matrixP
        for (int i = 0; i < factor; ++i) {
            term1[i] = 0;
        }
        for (int index = 0; index < starMatrix.tu; ++index) {
            float temp = 0;
            int row = starMatrix.data[index].i;
            int col = starMatrix.data[index].j;
            for (int k = 0; k < factor; ++k) {
                temp += (matrixP[k][row] * matrixQ[k][col]);
            }
            temp -= starMatrix.data[index].elem;
            for (int i = 0; i < factor; ++i) {
                term1[i] += (temp * matrixQ[i][col]);
            }
            
            // 如果一行计算结束:下一行的起始位置是否等于当前位置加一
            if (starMatrix.rpos[row+1] == (index+1)) {
                for (int i = 0; i < factor; ++i) {
                    term2[i] = lamda * matrixP[i][row];
                }
                
//                for (int i = 0; i < factor; ++i) {
//                    cout << term1[i] << "\t" << term2[i] << endl;
//                }
                // 梯度下降
                for (int i = 0; i < factor; ++i) {
                    matrixP[i][row] -= learnRate * (term1[i] + term2[i]);
                }
                
                for (int i = 0; i < factor; ++i) {
                    term1[i] = 0;
                }
            }
        }
        
        // 计算matrixQ
        for (int i = 0; i < factor; ++i) {
            term1[i] = 0;
        }
        for (int index = 0; index < transposeStarMatrix.tu; ++index) {
            float temp = 0;
            int row = transposeStarMatrix.data[index].i;
            int col = transposeStarMatrix.data[index].j;
            for (int k = 0; k < factor; ++k) {
                temp += (matrixP[k][col] * matrixQ[k][row]);
            }
            temp -= transposeStarMatrix.data[index].elem;
            for (int i = 0; i < factor; ++i) {
                term1[i] += (temp * matrixP[i][col]);
            }
            
            if (transposeStarMatrix.rpos[row + 1] == (index+1)) {
                for (int i = 0; i < factor; ++i) {
                    term2[i] = lamda * matrixQ[i][row];
                }
//                for (int i = 0; i < factor; ++i) {
//                    cout << term1[i] << "\t" << term2[i] << endl;
//                }
                // 梯度下降
                for (int i = 0; i < factor; ++i) {
                    matrixQ[i][row] -= learnRate * (term1[i]+term2[i]);
                }
                
                for (int i = 0; i < factor; ++i) {
                    term1[i] = 0;
                }
            }
        }

        // 计算RMSE
        float sum = 0;
        for (int index = 0; index < starMatrix.tu; ++index) {
            float temp = 0;
            int row = starMatrix.data[index].i;
            int col = starMatrix.data[index].j;
            for (int i = 0; i < factor; ++i) {
                temp += (matrixP[i][row] * matrixQ[i][col]);
            }
            sum += (temp - starMatrix.data[index].elem) * (temp - starMatrix.data[index].elem);
        }
        midRMSE = sqrt(sum/starMatrix.tu);
        ++count;
        if (midRMSE < 0.97) {
            cout << count << ":\t" << midRMSE << endl;
        }
        
    }//while(...)
    
}