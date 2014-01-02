//
//  BasicPMF.cpp
//  RecSys2013
//
//  Created by JTangWang on 13-7-7.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#include "BasicPMF.h"
#include "SparseMatrix.h"
#include "User.h"
#include "Business.h"
#include "Review.h"


#include <cmath>
#include <sstream>
#include <fstream>


using namespace std;


BasicPMF::BasicPMF(int uCount, int bCount, float lrate, int f)
:lamda(0.005), userCount(uCount), businessCount(bCount), learnRate(lrate), factor(f)
{
//    srand((unsigned int)clock());
    matrixP = new float[factor][UserSize];
    matrixQ = new float[factor][BusinessSize];
    for (int i = 0; i < factor; ++i) {
        for (int j = 0; j < UserSize; ++j) {
            matrixP[i][j] = sqrt((GlobalAvg)/(float)factor) + SmallRandom;
        }
    }
    for (int i = 0; i < factor; ++i) {
        for (int j = 0; j < BusinessSize; ++j) {
            matrixQ[i][j] = sqrt((GlobalAvg)/(float)factor) + SmallRandom;
        }
    }
}


void BasicPMF::compute(const SparseMatrix<float> &starMatrix, const SparseMatrix<float> &transposeStarMatrix, const int maxIterCount, const map<string, User> &userMap, const map<string, Business> &businessMap)
{    
    int count = 0;
    float *term1 = new float[factor];
    while(count < maxIterCount)
    {
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
                // 梯度下降
                for (int i = 0; i < factor; ++i) {
                    matrixP[i][row] -= learnRate * (term1[i] + lamda * matrixP[i][row]);
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
                // 梯度下降
                for (int i = 0; i < factor; ++i) {
                    matrixQ[i][row] -= learnRate * (term1[i] + lamda * matrixQ[i][row]);
                }
                
                for (int i = 0; i < factor; ++i) {
                    term1[i] = 0;
                }
            }
        }

        // 计算RMSE
//        float sum = 0;
//        for (int index = 0; index < starMatrix.tu; ++index) {
//            float temp = 0;
//            int row = starMatrix.data[index].i;
//            int col = starMatrix.data[index].j;
//            for (int i = 0; i < factor; ++i) {
//                temp += (matrixP[i][row] * matrixQ[i][col]);
//            }
//            sum += (temp - starMatrix.data[index].elem) * (temp - starMatrix.data[index].elem);
//        }
//        float midRMSE = sqrt(sum/starMatrix.tu);
//        if (midRMSE < 1.0) {
//            cout << count << ":\t" << midRMSE << endl;
//        }
        
#ifdef LocalTest
        if (count >= 40 && (count % 2 == 0)) {
            cout << count << "\t";
            map<string, Business> testBusinessMap;
            map<string, float> cityAvgMap;
            predict(userMap, businessMap, testBusinessMap, cityAvgMap);
        }
#endif
        
        ++count;
    }//while(...)
    delete [] term1;
    iterCount = count;
}

void BasicPMF::predict(const map<string, User> &userMap, const map<string, Business> &businessMap, const map<string, Business> &testBusinessMap, const map<string, float> &cityAvgMap)
{
    stringstream predictionFileName;
    
    ifstream submitionFile = ifstream(FolderName + "sampleSubmission.csv");
    predictionFileName << FolderName + "BasicSVD/BasicSVD_lrate" << learnRate << "_factor" << factor << "_iter" << iterCount << ".csv";
    
    ofstream predictionFile = ofstream(predictionFileName.str());
    if (submitionFile.is_open())
    {
        string line;
        
#ifndef LocalTest
        predictionFile << "RecommendationId,Stars" << endl;
        getline(submitionFile, line);
#endif
        int index = 0;
        int lfmCount = 0;
        int userAvgCount = 0;
        int businessAvgCount = 0;
        int globalCount = 0;
        while (!submitionFile.eof())
        {
            getline(submitionFile, line);
#ifdef LocalTest
            size_t start;
            start = line.find("\"user_id\"");
            string uid = line.substr(start+12, 22);
            string bid = line.substr(line.length() - 24, 22);
#else
            string uid = line.substr(0, 22);
            string bid = line.substr(23, 22);
#endif
            map<string, User>::const_iterator userIter = userMap.find(uid);
            map<string, Business>::const_iterator businessIter = businessMap.find(bid);
            float prediction = 0;
            
            if (userIter != userMap.end() && businessIter != businessMap.end()) {
                if (userIter->second.sequence > -1 && businessIter->second.sequence > -1) {
                    for (int i = 0; i < factor; ++i) {
                        prediction += (matrixP[i][userIter->second.sequence] * matrixQ[i][businessIter->second.sequence]);
                    }
                    ++lfmCount;
                }
                else
                {
                    // TODO:使用用户平均值还是商家平均值？取review_count高的平均值吗？
                    // 商家平均值优先：1.24759
                    // 用户平均值优先：1.24759
                    // review_count优先：1.24759
                    if (userIter->second.reviewCount > businessIter->second.reviewCount) {
                        prediction = userIter->second.avgStar;
                    }
                    else
                    {
                        prediction = businessIter->second.avgStar;
                    }
                    ++businessAvgCount;
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
                // TODO:根据商家所在的类别使用不同的平均值
                map<string, Business>::const_iterator testBusinessIter = testBusinessMap.find(bid);
                map<string, float>::const_iterator cityAvgIter = cityAvgMap.find(testBusinessIter->second.city);
                if (testBusinessIter != testBusinessMap.end() && cityAvgIter != cityAvgMap.end()) {
                    //prediction = cityAvgIter->second;
                    prediction = GlobalAvg;
                }
                else
                {
                    prediction = GlobalAvg;
                }
                ++globalCount;
            }
            
            
            if (prediction > 5) {
                prediction = 5;
            }
            if (prediction < 1) {
                prediction = 1;
            }
            predictionFile << ++index << "," << prediction << endl;
        }
//        cout << "LFM Count:" << lfmCount << "\tUser Avg Count:" << userAvgCount << "\tBusiness Avg Count:" << businessAvgCount << "\tGlobal Count:" << globalCount << endl;
    }
    submitionFile.close();
    predictionFile.close();
    
#ifdef LocalTest
    cout << "RMSE:\t" << computeRMSE(predictionFileName.str()) << endl;
#endif
}