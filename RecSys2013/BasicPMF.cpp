//
//  BasicPMF.cpp
//  RecSys2013
//
//  Created by JTangWang on 13-7-7.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#include "BasicPMF.h"

using namespace std;


BasicPMF::BasicPMF(int userCount, int businessCount, int Factor)
{
    srand((unsigned int)clock());
    matrixP = new float[Factor][UserSize];
    matrixQ = new float[Factor][BusinessSize];
    for (int i = 0; i < Factor; ++i) {
        for (int j = 0; j < UserSize; ++j) {
            matrixP[i][j] = (rand()%5)+1;
        }
    }
    for (int i = 0; i < Factor; ++i) {
        for (int j = 0; j < BusinessSize; ++j) {
            matrixQ[i][j] = (rand()%5)+1;
        }
    }
}


void BasicPMF::compute(const SparseMatrix &starMatrix)
{
    double midRMSE = 10.0f;
    double[][] oldRTMatrix = VectorUtil.computeMatrixUS(MatrixU, MatrixS); //根据随机数生成的初始目标矩阵
    
    int count = 0;
    while((midRMSE > iterThreshold) && (count < maxIterNum)) {   //只要其中一个条件不满足就停止
        
        //计算MatrixU
        for(int i = 0; i < UserSize; i++) {
            double[] vectorU = VectorUtil.assignVector(MatrixU, i);//第i列
            double[] term1 = VectorUtil.initVector(D);
            
            for(int j = 0; j < ServiceSize; j++) {
                
                if(SparseRTMatrix[i][j] != 0) {
                    double[] vectorS = VectorUtil.assignVector(MatrixS, j);
                    
                    double term1Num = VectorUtil.multipleVector(vectorU, vectorS);
                    term1Num = term1Num - SparseRTMatrix[i][j];
                    double[] term1Vector = VectorUtil.multipleVector(term1Num, vectorS);
                    
                    term1 = VectorUtil.sumVector(term1, term1Vector);
                }
                
            }//for(j...)
            
            double[] term2 = VectorUtil.multipleVector(Lamda, vectorU);
            
            double[] DerivativeVector = VectorUtil.initVector(D);
            DerivativeVector = VectorUtil.sumVector(term1, term2);
            
            //梯度下降
            for(int d = 0; d < D; d++) {
                MatrixU[d][i] = MatrixU[d][i] - Lrate * DerivativeVector[d];
            }
        }//for(i...)
        
        //计算MatrixS
        for(int j = 0; j < ServiceSize; j++) {
            
            double[] vectorS = VectorUtil.assignVector(MatrixS, j);
            double[] term1 = VectorUtil.initVector(D);
            
            //every user
            for(int m = 0; m < UserSize; m++) {
                
                if(SparseRTMatrix[m][j] != 0) {
                    double[] vectorU = VectorUtil.assignVector(MatrixU, m);
                    
                    double term1Num = VectorUtil.multipleVector(vectorU, vectorS);
                    term1Num = term1Num - SparseRTMatrix[m][j];
                    
                    double[] term1Vector = VectorUtil.multipleVector(term1Num, vectorU);
                    term1 = VectorUtil.sumVector(term1, term1Vector);
                }//if(...)
                
            }//for(...)
            
            double[] term2 = VectorUtil.multipleVector(Lamda, vectorS);
            
            double[] DerivativeVector = VectorUtil.initVector(D);
            DerivativeVector = VectorUtil.sumVector(term1, term2);
            
            for(int d = 0; d < D; d++) {
                MatrixS[d][j] = MatrixS[d][j] - Lrate * DerivativeVector[d];
            }
        }//for(j...)
        
        //Matrix
        double[][] newRTMatrix = VectorUtil.computeMatrixUS(MatrixU, MatrixS);
        
        midRMSE = VectorUtil.computeMidRMSE(newRTMatrix, oldRTMatrix);
        
        System.out.println("midRMSE : " + midRMSE + " ; count : " + count);
        
        oldRTMatrix = newRTMatrix;
        count++;
        
    }//while(...)
    
}