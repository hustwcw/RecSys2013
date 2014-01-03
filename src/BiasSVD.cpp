//
//  BiasSVD.cpp
//  RecSys2013
//
//  Created by JTangWang on 13-7-7.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#include "BiasSVD.h"
#include "SparseMatrix.h"
#include "User.h"
#include "Business.h"
#include "Review.h"


#include <cmath>
#include <sstream>
#include <fstream>
#include <iomanip>

using namespace std;


BiasSVD::BiasSVD(int uCount, int bCount, float lrate, int f)
:lamda(0.001), userCount(uCount), businessCount(bCount), learnRate(lrate), factor(f)
{
    // 根据经验，随机数需要和 1/sqrt(F) 成正比
    matrixP = new float[factor][UserSize];
    matrixQ = new float[factor][BusinessSize];
    for (int i = 0; i < factor; ++i) {
        for (int j = 0; j < UserSize; ++j) {
            matrixP[i][j] = SmallRandom/sqrt(i+1.0);
        }
    }
    for (int i = 0; i < factor; ++i) {
        for (int j = 0; j < BusinessSize; ++j) {
            matrixQ[i][j] = SmallRandom/sqrt(i+1.0);
        }
    }
    for (int i = 0; i < UserSize; ++i) {
        ubias[i] = 0;
    }
    for (int i = 0; i < BusinessSize; ++i) {
        bbias[i] = 0;
    }
}

void BiasSVD::compute(const SparseMatrix<float> &starMatrix, const SparseMatrix<float> &transposeStarMatrix, const int maxIterCount, const map<string, User> &userMap, const map<string, Business> &businessMap)
{    
    int count = 0;
    float *term1 = new float[factor];
    float tempLearnRate = learnRate;
    float oldLoss = INTMAX_MAX;
    while(count < maxIterCount)   //只要其中一个条件不满足就停止迭代
    {
        float derivativeUbias = 0;
        float derivativeBbias = 0;
        // 计算matrixP & ubias
        for (int i = 0; i < factor; ++i) {
            term1[i] = 0;
        }
        for (int index = 0; index < starMatrix.tu; ++index) {
            float temp = 0;
            int row = starMatrix.data[index].i; // user
            int col = starMatrix.data[index].j; // business
            for (int k = 0; k < factor; ++k) {
                temp += (matrixP[k][row] * matrixQ[k][col]);
            }
            temp += (GlobalAvg + ubias[row] + bbias[col]);
            temp -= starMatrix.data[index].elem;
            for (int i = 0; i < factor; ++i) {
                term1[i] += (temp * matrixQ[i][col]);
            }
            derivativeUbias += temp;
            
            // 如果一行计算结束:下一行的起始位置是否等于当前位置加一
            if (starMatrix.rpos[row+1] == (index+1)) {
                // 梯度下降
                for (int i = 0; i < factor; ++i) {
                    matrixP[i][row] -= tempLearnRate * (term1[i] + lamda * matrixP[i][row]);
                }
                ubias[row] -= (tempLearnRate * derivativeUbias);
                
                // 临时变量清零
                for (int i = 0; i < factor; ++i) {
                    term1[i] = 0;
                }
                derivativeUbias = 0;
            }
        }
        
        // 计算matrixQ
        for (int i = 0; i < factor; ++i) {
            term1[i] = 0;
        }
        for (int index = 0; index < transposeStarMatrix.tu; ++index) {
            float temp = 0;
            int row = transposeStarMatrix.data[index].i;    // business
            int col = transposeStarMatrix.data[index].j;    // user
            for (int k = 0; k < factor; ++k) {
                temp += (matrixP[k][col] * matrixQ[k][row]);
            }
            temp += (GlobalAvg + ubias[col] + bbias[row]);
            temp -= transposeStarMatrix.data[index].elem;
            for (int i = 0; i < factor; ++i) {
                term1[i] += (temp * matrixP[i][col]);
            }
            derivativeBbias += temp;
            
            if (transposeStarMatrix.rpos[row + 1] == (index+1)) {
                // 梯度下降
                for (int i = 0; i < factor; ++i) {
                    matrixQ[i][row] -= tempLearnRate * (term1[i] + lamda * matrixQ[i][row]);
                }
                bbias[row] -= (tempLearnRate * derivativeBbias);
                
                // 临时变量清零
                for (int i = 0; i < factor; ++i) {
                    term1[i] = 0;
                }
                derivativeBbias = 0;
            }
        }
        
        
        // 计算损失函数
        float sum = 0.0;
        for (int index = 0; index < starMatrix.tu; ++index) {
            float temp = 0;
            int row = starMatrix.data[index].i;
            int col = starMatrix.data[index].j;
            for (int i = 0; i < factor; ++i) {
                temp += (matrixP[i][row] * matrixQ[i][col]);
            }
            temp += (GlobalAvg + ubias[row] + bbias[col]);
            sum += (temp - starMatrix.data[index].elem) * (temp - starMatrix.data[index].elem)*1.0;
        }
        
        float sumP = 0;
        float sumQ = 0;
        for (int i = 0; i < factor; ++i) {
            for (int j = 0; j < userCount; ++j) {
                sumP += (matrixP[i][j] * matrixP[i][j]);
            }
            for (int j = 0; j < businessCount; ++j) {
                sumQ += (matrixQ[i][j] * matrixQ[i][j]);
            }
        }
        float loss = sum + lamda*(sumP+sumQ);
        if(loss < oldLoss) // 损失越来越小
        {
            cout << count << ":\t" << loss << endl;
            oldLoss = loss;
        }
        else
        {
            break;
        }
        ++count;
    }//while(...)
    delete [] term1;
    iterCount = count;
}


void BiasSVD::predict(const map<string, User> &userMap, const map<string, Business> &businessMap, const map<string, Business> &testBusinessMap)
{
    stringstream predictionFileName;
    
    ifstream  testReviewFile = ifstream(FolderName + "final_test_set/final_test_set_review.json");
    predictionFileName << FolderName + "BiasSVD/BiasSVD_lamda" << lamda << "_lrate" << learnRate << "_factor" << factor << "_iter" << iterCount << ".csv";
    
    ofstream predictionFile = ofstream(predictionFileName.str());
    // 根据result和UserMap、BusinessMap中的评分平均值计算最终的评分
    if (testReviewFile.is_open())
    {
        string line;
        
        predictionFile << "review_id,stars" << endl;
        int lfmCount = 0;
        int userAvgCount = 0;
        int businessAvgCount = 0;
        int globalCount = 0;
        while (!testReviewFile.eof())
        {
            getline(testReviewFile, line);
            string uid = line.substr(13, 22);
            string reviewid = line.substr(52, 22);
            string bid = line.substr(93, 22);
            map<string, User>::const_iterator userIter = userMap.find(uid);
            map<string, Business>::const_iterator businessIter = businessMap.find(bid);
            float prediction = 0;
            
            if (userIter != userMap.end() && businessIter != businessMap.end()) {
                if (userIter->second.sequence > -1 && businessIter->second.sequence > -1) {
                    for (int i = 0; i < factor; ++i) {
                        prediction += (matrixP[i][userIter->second.sequence] * matrixQ[i][businessIter->second.sequence]);
                    }
                    prediction += (GlobalAvg + ubias[userIter->second.sequence] + bbias[businessIter->second.sequence]);
//                    prediction = ((int)(prediction * 2 + 0.5)) / 2.0;
                    ++lfmCount;
                }
                else
                {
                    // 用户或者商家在训练集，但是其中一个没有在review中
                    cout << "不存在这种情况" << endl;
                }
            }
            else if (userIter != userMap.end())
            {
                prediction = userIter->second.avgStar;
                ++userAvgCount;
            }
            else if (businessIter != businessMap.end())
            {
                prediction = businessIter->second.avgStar;
                ++businessAvgCount;
            }
            else
            {
                map<string, Business>::const_iterator testBusinessIter = testBusinessMap.find(bid);
                prediction = testBusinessIter->second.cateAvgStar;
                if (prediction < 1) {
                    cout << prediction << endl;
                }
                ++globalCount;
            }
            
//            prediction = ((int)(prediction * 2 + 0.5)) / 2.0;
            
            if (prediction > 5 && prediction != 10) {
                prediction = 5;
            }
            if (prediction < 1) {
                prediction = 1;
            }
            predictionFile << reviewid << "," << prediction << endl;
        }
        cout << "LFM Count:" << lfmCount << "\tUser Avg Count:" << userAvgCount << "\tBusiness Avg Count:" << businessAvgCount << "\tGlobal Count:" << globalCount << endl;
    }
    testReviewFile.close();
    predictionFile.close();
    
//#ifdef LocalTest
//    cout << "RMSE:\t" << computeRMSE(predictionFileName.str()) << endl;
//#endif
}
