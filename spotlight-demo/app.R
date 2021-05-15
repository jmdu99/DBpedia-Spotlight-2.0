library(shiny)
library(shinythemes)
library(markdown)
library(DT)

source("querySpotlight.R")


####################################
# User Interface                   #
####################################
ui <- fluidPage(theme = shinytheme("spacelab"),
                navbarPage("DBpedia Spotlight 2.0",
                           
                           tabPanel("Home",
                                    # Input values
                                    sidebarPanel(
                                      HTML("<h3>Input parameters</h3>"),
                                      selectInput("language","Language",choices = list("English" = 1, "Spanish" = 2),selected = 1),
                                      
                                      sliderInput("confidence", 
                                                  label = "Confidence score for disambiguation / linking", 
                                                  value = 0.5, 
                                                  min = 0, 
                                                  max = 1),
                                      
                                      
                                      textInput("types", "Types filter (Eg.DBpedia:Country,Wikidata:Q6256,Schema:Country)", value = "", placeholder = "Can be empty"),
                                      
                                      selectInput("policy","Policy: (whitelist) select all entities that have the same type; (blacklist) - select all entities that have not the same type",choices = list("whitelist" = "whitelist", "blacklist" = "blacklist"),selected = "whitelist"),
                                      
                                      textInput("text", "Text to be annotated", value = ""),
                                      
                                      actionButton("submitbutton", 
                                                   "ANNOTATE", 
                                                   class = "btn btn-primary")
                                    ),
                                    
                                    mainPanel(
                                      tags$label(h3('Annotations')), # Status/Output Text Box
                                      dataTableOutput('tabledata') # Results table
                                    ) # mainPanel()
                                    
                           ), #tabPanel(), Home
                           
                           tabPanel("About", 
                                    div(includeMarkdown("about.md"), 
                                        align="justify")
                           ) #tabPanel(), About
                           
                ) # navbarPage()
) # fluidPage()


####################################
# Server                           #
####################################



server <- function(input, output, session) {
  
  datasetInput <- reactive({  
    query_spotlight(input$language,input$text,input$confidence,input$types,input$policy)
  })
  
  output$tabledata <- renderDataTable(escape = FALSE,{
    if (input$submitbutton>0) {
      isolate(datasetInput())
    }
  })
  
}


####################################
# Create Shiny App                 #
####################################
shinyApp(ui = ui, server = server)