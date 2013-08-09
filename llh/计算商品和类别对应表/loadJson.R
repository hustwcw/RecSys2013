setwd("E:/Recsys/计算商品和类别对应表")

require(rjson)

jsonFile <- "yelp_training_set_business.json"
conR <- file(jsonFile, "r")
lines = readLines(conR,n=10000000)
close(conR)

conW <- file("businessId2Categories.csv", "w")
cat("businessId,Categories", "\n",sep="", file=conW)
categoriesTable <- data.frame()

for(tmpLine in lines){ 
  jsonData <- fromJSON(tmpLine)
#要注意的是这里每次读一行，而且$categories有多个 读一行 处理一个
  tmpBusinessId <- jsonData["business_id"][[1]]
  tmpCategories <- jsonData$categories
  
#Books, Mags, Music & Video 逗号替换成了空
#Beer, Wine & Spirits
#Used, Vintage & Consignment 
  for(varCategories in tmpCategories){
    cat(tmpBusinessId, ",",varCategories, "\n", sep="", file=conW)
  }
}
  
close(conW)
# categoriesTable$Categories[categoriesTable$BusinessId == "rncjoVoEFUJGCUoC1JgnUA"]

