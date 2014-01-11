//
//  CF_PCC.h
//  RecSys2013
//
//  Created by JTangWang on 13-8-12.
//  Copyright (c) 2013年 JTangWang. All rights reserved.
//

#ifndef __RecSys2013__CF_PCC__
#define __RecSys2013__CF_PCC__

#include <iostream>
#include <map>
#include "Util.h"


template<class T>
class SparseMatrix;
class User;
class Business;


void TestCFPCC(const SparseMatrix<std::pair<float, int> > &sparseUBMatrix,
               const SparseMatrix<std::pair<float, int> > &sparseBUMatrix,
               std::map<std::string, User> &userMap,
               std::map<std::string, Business> &businessMap,
               const std::multimap<std::string, std::string> &predictionUBMap,
               const std::multimap<std::string, std::string> &predictionBUMap);

#endif /* defined(__RecSys2013__CF_PCC__) */
