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
    public String databaseModelName; // x-database-model-name
    public String plainClassName;

    // Protocols

    public Boolean isProtocolUUIDType; // x-protocol-uuid-type
    public Boolean isProtocolSortOrderType; // x-protocol-sort-order-type
    public Boolean isProtocolNameType; // x-protocol-name-type
    public Boolean isProtocolSoftDeletableType; // x-protocol-soft-deleteable-type
    
    //Testing 

    public Boolean isExcludedFromTests; //x-exclude-from-tests

    //Other

    // Builds a convenience initializer with required variables
    public Boolean isInitRequired; // x-init-required

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
            this.isProtocolUUIDType = (Boolean) jsonData.get("x-protocol-uuid-type");
            this.isProtocolSortOrderType = (Boolean) jsonData.get("x-protocol-sort-order-type");
            this.isProtocolNameType = (Boolean) jsonData.get("x-protocol-name-type");
            this.isProtocolSoftDeletableType = (Boolean) jsonData.get("x-protocol-soft-deleteable-type");

            //Testing
            this.isExcludedFromTests = (Boolean) jsonData.get("x-exclude-from-tests");

            //Other
            this.isInitRequired = (Boolean) jsonData.get("x-init-required");
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