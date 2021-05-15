library(httr)
library(jsonlite)
library(dplyr)

base_en <- "http://localhost:2222/rest/"
base_es <- "http://localhost:2222/rest/"

endpoint <- "annotate"

query_spotlight <- function(language, text, confidence, types, policy){
  
  if(language == 1){
    
    base <- base_en
  }
  
  if(language == 2){
    
    base <- base_es
  }
  
  
  text <- URLencode(text)
  
  call <- paste(base,endpoint,"?","text","=", text,"&","confidence","=", confidence,"&","types","=",types,"&","policy","=",policy,sep="")
  
  
  # API Call
  
  get_annotations <- GET(call)
  get_annotations_text <- content(get_annotations, "text")
  get_annotations_json <- fromJSON(get_annotations_text, flatten = TRUE)
  get_annotations_df <- as.data.frame(get_annotations_json)
  
  df_filtered <- rename(get_annotations_df[c(10,7)],"Resource" = "Resources..URI", "Surface Form" = "Resources..surfaceForm")
  
  df_filtered$Resource = paste0("<a href='",  df_filtered$Resource,"' target='_blank'>",  df_filtered$Resource,"</a>")
  df_filtered
}


