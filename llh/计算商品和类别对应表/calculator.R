setwd("E:/Recsys/计算商品和类别对应表")

IdTable <- read.csv("businessId2Categories.csv", head=T);
CateAvgStarTable <- read.csv("CateAvgStar.txt", head=T);
CateTFIDFInfoTable <- read.csv("CateTFIDFInfo.csv", head=T);
trainBusiAvgInfosTable <- read.csv("trainBusiAvgInfos.csv", head=T)

conW <- file("wResult.csv", "w")
cat("businessId,AvgFromReviews,wIDF,wAvg,wRM,wBase", "\n",sep="", file=conW)

#全局的两个值
GlobalAvg <- 3.766723
meanMRSE <- 1.217

i <- 1
while(i<=11537){
  BusinessIdVar <- trainBusiAvgInfosTable[i, "BusinessId"]
  BusinessIdVar <- as.character(BusinessIdVar)
  AvgFromReviewsVar <- trainBusiAvgInfosTable[i, "AvgFromReviews"]
#  ReviewCntVar <- trainBusiAvgInfosTable[i, "ReviewCnt"]
  
  
  CateList <- IdTable$Categories[IdTable$businessId==BusinessIdVar]

  wIDF <- 0
  wAll1 <- 0
  
  wAvg <- 0
  wn <- 0
  
  wRM <- 0
  wAll2 <- 0
  
  wBase <- 0
  wAll3 <- 0
  for(VarCate in CateList){
#得到类别的统计数据
    CateAvgVar <- CateAvgStarTable$AvgStars[CateAvgStarTable$Category==VarCate]
    TotalReviewCntVar <- CateAvgStarTable$TotalReviewCnt[CateAvgStarTable$Category==VarCate]
    MRSEVar <- CateAvgStarTable$MRSE[CateAvgStarTable$Category==VarCate]
    IDFVar <- CateTFIDFInfoTable$IDF[CateTFIDFInfoTable$Category==VarCate]
    
#计算其IDF加权估计值(有的business类别为[])
    
    wAll1 <- wAll1 + IDFVar
    wIDF <- wIDF + IDFVar * CateAvgVar
    
#单纯均值  
    wn <- wn + 1
    wAvg <- wAvg + CateAvgVar
    
#reviewCnt取log10后除以MRSE的值
    if(MRSEVar!=0){
      wTmp <- log10(TotalReviewCntVar)/MRSEVar
    
      wAll2 <- wAll2 + wTmp
      wRM <- wRM + wTmp * CateAvgVar
    }
#与上面一样，但是对于MRSE大于全局平均时不计入计算
#    如果所有类别都不计入计算则取所有item的平均值    
    if(MRSEVar <= meanMRSE){
      wAll3 <- wAll3 + wTmp
      wBase <- wBase + wTmp * CateAvgVar     
    }
  }
  
  if(wIDF!=0){
    wIDF <- wIDF/wAll1
  }
  else{
    wIDF <- GlobalAvg
  }  
  
  if(wAvg!=0){
    wAvg <- wAvg/wn
  }
  else{
    wAvg <- GlobalAvg
  }
  
  if(wRM!=0){
    wRM <- wRM/wAll2
  }
  else{
    wRM <- GlobalAvg
  }
  
  if(wBase!=0){
    wBase <- wBase/wAll3
  }
  else{
    wBase <- GlobalAvg
  }

#写文件操作
  cat(BusinessIdVar,",",AvgFromReviewsVar,",",wIDF,",",wAvg,",",wRM,",",wBase,"\n", sep="", file=conW)
  
  i <- i+1
}

close(conW)
  