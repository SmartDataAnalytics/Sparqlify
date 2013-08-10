Prefix xsd: <http://www.w3.org/2001/XMLSchema#>
Prefix dc:  <http://purl.org/dc/terms/>
Prefix sb: <http://ns.aksw.org/spring/batch/>

Prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
Prefix ns: <http://example.org/resource/>



Create View batchJobExecution As
  Construct {
    ?s
      a sb:JobExecution ;
      sb:id ?id ; 
      sb:version ?version ;
      sb:createTime ?createTime ;
      sb:startTime ?startTime ;
      sb:endTime ?endTime ;
      sb:status ?status ;
      sb:exitCode ?exitCode ;
      sb:exitMessage ?exitMessage ;
      sb:lastUpdated ?lastUpdated .
      
    ?jobInstance
      sb:jobExecution ?s .
  }
  With
    ?s           = uri(ns:jobExecution, ?JOB_EXECUTION_ID)
    ?id          = typedLiteral(?JOB_EXECUTION_ID, xsd:long)
    ?version     = typedLiteral(?VERSION, xsd:long)
    ?jobInstance = uri(ns:jobInstance, ?JOB_INSTANCE_ID)
    ?createTime  = typedLiteral(?CREATE_TIME, xsd:dateTime)
    ?startTime   = typedLiteral(?START_TIME, xsd:dateTime)
    ?endTime     = typedLiteral(?END_TIME, xsd:dateTime)
    ?status      = plainLiteral(?STATUS)
    ?exitCode    = plainLiteral(?EXIT_CODE)
    ?exitMessage = plainLiteral(?EXIT_MESSAGE)
    ?lastUpdated = typedLiteral(?LAST_UPDATED, xsd:dateTime)     
  From
     BATCH_JOB_EXECUTION


/*
Create View batchJobExecutionContext As
  Construct {
      ?s
        a sb:JobExecutionContext ;
        sb:shortContext ?shortContext ;
        sb:serializedContext ?serializedContext .
        
      ?jobExecution
        sb:jobExecutionContext ?s .
  }
  With
      ?s                 = uri(ns:jobExecutionContext, ?JOB_EXECUTION_ID)
      ?jobExecution      = uri(ns:jobExecution, ?JOB_EXECUTION_ID)
      ?shortContext      = typedLiteral(?SHORT_CONTEXT, xsd:string)
      ?serializedContext = typedLiteral(?SERIALIZED_CONTEXT, xsd:string)
  From
    BATCH_JOB_EXECUTION_CONTEXT 
*/

Create View batchJobExecutionContext As
  Construct {
      ?jobExecution
        sb:shortContext ?shortContext ;
        sb:serializedContext ?serializedContext .
  }
  With
      ?jobExecution      = uri(ns:jobExecution, ?JOB_EXECUTION_ID)
      ?shortContext      = typedLiteral(?SHORT_CONTEXT, xsd:string)
      ?serializedContext = typedLiteral(?SERIALIZED_CONTEXT, xsd:string)
  From
    BATCH_JOB_EXECUTION_CONTEXT 



Create View batchJobExecutionParams As
  Construct {
    ?s
      a sb:JobExecutionParam ;
      sb:typeCd ?typeCd ;
      sb:keyName ?keyName ;
      sb:value ?stringVal ;
      sb:value ?dateVal ;
      sb:value ?longVal ;
      sb:value ?doubleVal ;
      sb:identifying ?identifying .
      
    ?jobExecution
      sb:param ?s .
      
  }
  With
    ?s           = uri(ns:jobExecution, ?JOB_EXECUTION_ID)
    ?typeCd      = plainLiteral(?TYPE_CD)
    ?keyName     = plainLiteral(?KEY_NAME)
    ?stringVal   = typedLiteral(?STRING_VAL, xsd:string)
    ?dateVal     = typedLiteral(?DATE_VAL, xsd:dateTime)
    ?longVal     = typedLiteral(?LONG_VAL, xsd:long)
    ?doubleVal   = typedLiteral(?DOUBLE_VAL, xsd:double)
    ?identifying = typedLiteral(?IDENTIFYING, xsd:boolean)
  From
    BATCH_JOB_EXECUTION_PARAMS
     
     
Create View batchJobInstance As
  Construct {
    ?s
      a sb:JobInstance ;
      sb:id ?id ;
      rdfs:label ?name ;
      sb:version ?version ;
      sb:key ?key .
  }
  With
    ?s       = uri(ns:jobInstance, ?JOB_INSTANCE_ID)
    ?id      = typedLiteral(?JOB_INSTANCE_ID, xsd:long)
    ?version = typedLiteral(?VERSION, xsd:long)
    ?name    = plainLiteral(?JOB_NAME)
    ?key     = plainLiteral(?JOB_KEY)
  From
    BATCH_JOB_INSTANCE

 
Create View batchStepExecution As
  Construct {
    ?s
      a sb:StepExecution ;
      sb:id ?id ; 
      sb:version ?version ;
      rdfs:label ?stepName ;
      
      sb:startTime ?startTime ;
      sb:endTime ?endTime ;
      sb:status ?status ;
      
      sb:commitCount ?commitCount ;
      sb:readCount ?readCount ;
      sb:filterCount ?filterCount ;
      sb:writeCount ?writeCount ;
      sb:readSkipCount ?readSkipCount ;
      sb:writeSkipCount ?writeSkipCount ;
      sb:processSkipCount ?processSkipCount ;
      sb:rollbackCount ?rollbackCount ;
      
      sb:exitCode ?exitCode ;
      sb:exitMessage ?exitMessage ;
      sb:lastUpdated ?lastUpdated .
     
   ?jobExecution
     sb:stepExecution ?s .
  }
  With
    ?s           = uri(ns:stepExecution, ?STEP_EXECUTION_ID)
    ?id          = typedLiteral(?STEP_EXECUTION_ID, xsd:long)
    ?version     = typedLiteral(?VERSION, xsd:long)
    ?stepName    = plainLiteral(?STEP_NAME)
    //?jobInstance = typedLiteral(?JOB_INSTANCE_ID, xsd:long)
    ?startTime   = typedLiteral(?START_TIME, xsd:dateTime)
    ?endTime     = typedLiteral(?END_TIME, xsd:dateTime)
    ?status      = plainLiteral(?STATUS)
    ?commitCount = typedLiteral(?COMMIT_COUNT, xsd:long)
    ?readCount   = typedLiteral(?READ_COUNT, xsd:long)
    ?filterCount = typedLiteral(?FILTER_COUNT, xsd:long)
    ?writeCount  = typedLiteral(?WRITE_COUNT, xsd:long)
    ?readSkipCount  = typedLiteral(?READ_SKIP_COUNT, xsd:long)
    ?writeSkipCount  = typedLiteral(?WRITE_SKIP_COUNT, xsd:long)
    ?processSkipCount  = typedLiteral(?PROCESS_SKIP_COUNT, xsd:long)
    ?rollbackCount  = typedLiteral(?ROLLBACK_COUNT, xsd:long)
    ?exitCode    = plainLiteral(?EXIT_CODE)
    ?exitMessage = plainLiteral(?EXIT_MESSAGE)
    ?lastUpdated = typedLiteral(?LAST_UPDATED, xsd:dateTime)     
    
    ?jobExecution = uri(ns:jobExecution, ?JOB_EXECUTION_ID)
  From
     BATCH_STEP_EXECUTION


Create View batchJobExecutionContext As
  Construct {
      ?stepExecution
        sb:shortContext ?shortContext ;
        sb:serializedContext ?serializedContext .
  }
  With
      ?stepExecution     = uri(ns:stepExecution, ?STEP_EXECUTION_ID)
      ?shortContext      = typedLiteral(?SHORT_CONTEXT, xsd:string)
      ?serializedContext = typedLiteral(?SERIALIZED_CONTEXT, xsd:string)
  From
    BATCH_STEP_EXECUTION_CONTEXT 

/*
Create View batchJobExecutionContext As
  Construct {
      ?s
        a sb:StepExecutionContext ;
        sb:shortContext ?shortContext ;
        sb:serializedContext ?serializedContext .
                
      ?stepExecution
        sb:stepExecutionContext ?s .
  }
  With
      ?s                 = uri(ns:stepExecutionContext, ?STEP_EXECUTION_ID)
      ?stepExecution      = uri(ns:stepExecution, ?STEP_EXECUTION_ID)
      ?shortContext      = typedLiteral(?SHORT_CONTEXT, xsd:string)
      ?serializedContext = typedLiteral(?SERIALIZED_CONTEXT, xsd:string)
  From
    BATCH_STEP_EXECUTION_CONTEXT 
*/
 

 