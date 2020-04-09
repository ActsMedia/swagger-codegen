package io.swagger.codegen.languages;

import io.swagger.codegen.CodegenModelFactory;
import io.swagger.codegen.CodegenModelType;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.DefaultCodegen;
import io.swagger.codegen.SupportingFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.google.common.base.Strings;
import java.util.regex.Pattern;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class DatabaseCodegenModel extends CodegenModel {

    //Database Additions

    // Whether this model should be built during database generation
    public Boolean isDatabaseModel; // x-database-model
    // The Type name for the database model
    public String databaseModelName; // x-database-model-name
    // The Type name for the non-database model
    public String plainClassName;

    // Protocols

    // Indicates whether this model has a property that should be used as primary ID when linking/identifying instances of this Type
    public Boolean databaseIsIdentifiable; // x-database-is-identifiable

    // Indicates whether this model has database relationships that should be generated
    public Boolean databaseShouldGenerateRelationLinks; // x-database-should-generate-relation-links

    // Whether an adherence should be generated for SoftDeletable
    public Boolean isProtocolSoftDeletableType; // x-protocol-soft-deleteable-type

    public Boolean useDefaultPropertyNames; // x-use-default-property-names
    
    //Testing 

    public Boolean isExcludedFromTests; //x-exclude-from-tests

    public void processCustomProperties(boolean isDatabaseGeneration) {
        
        // Check JSON for custom code
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> jsonData = mapper.readValue(this.modelJson, Map.class);
            
            if(!jsonData.containsKey("x-database-model")) {
                //In this case the model should be generated normally
                return;
            }
            //Database
            this.isDatabaseModel = (Boolean) jsonData.get("x-database-model");
            this.databaseModelName = (String) jsonData.get("x-database-model-name");
            if(isDatabaseGeneration) {
                this.plainClassName = this.classname;
                this.classFilename = this.databaseModelName;
                this.classname = this.databaseModelName;
            }
            //Protocols
            this.databaseIsIdentifiable = (Boolean) jsonData.get("x-database-is-identifiable");
            this.databaseShouldGenerateRelationLinks = (Boolean) jsonData.get("x-database-should-generate-relation-links");
            this.isProtocolSoftDeletableType = (Boolean) jsonData.get("x-protocol-soft-deleteable-type");

            //Testing
            this.isExcludedFromTests = (Boolean) jsonData.get("x-exclude-from-tests");

            //Other
            this.useDefaultPropertyNames = (Boolean) jsonData.get("x-use-default-property-names");
            if(isDatabaseGeneration) {
                imports.add("Infrastructure");

                imports.add("annotations");
                imports.add("ToMany");
                imports.add("ToOne");
                imports.add("SerializedName");
            }

        } catch (IOException e) {}
    }
}