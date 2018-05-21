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

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class KotlinObjectBoxCodegen extends AbstractKotlinCodegen {

    

    public static final String DATE_LIBRARY = "dateLibrary";
    protected CodegenConstants.ENUM_PROPERTY_NAMING_TYPE enumPropertyNaming = CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.camelCase;
    static Logger LOGGER = LoggerFactory.getLogger(KotlinObjectBoxCodegen.class);

    protected String dateLibrary = DateLibrary.JAVA8.value;

    public enum DateLibrary {
        STRING("string"),
        THREETENBP("threetenbp"),
        JAVA8("java8");

        public final String value;

        DateLibrary(String value) {
            this.value = value;
        }
    }

    /**
     * Constructs an instance of `KotlinObjectBoxCodegen`.
     */
    public KotlinObjectBoxCodegen() {
        super();

        artifactId = "kotlin-ObjectBox";
        packageName = "io.swagger.client";

        outputFolder = "generated-code" + File.separator + "kotlin-ObjectBox";
        modelTemplateFiles.put("model.mustache", ".kt");
        apiTemplateFiles.put("api.mustache", ".kt");
        modelDocTemplateFiles.put("model_doc.mustache", ".md");
        apiDocTemplateFiles.put("api_doc.mustache", ".md");
        embeddedTemplateDir = templateDir = "kotlin-ObjectBox";
        // apiPackage = packageName + ".apis";
        // modelPackage = packageName + ".models";

        CliOption dateLibrary = new CliOption(DATE_LIBRARY, "Option. Date library to use");
        Map<String, String> dateOptions = new HashMap<>();
        dateOptions.put(DateLibrary.THREETENBP.value, "Threetenbp");
        dateOptions.put(DateLibrary.STRING.value, "String");
        dateOptions.put(DateLibrary.JAVA8.value, "Java 8 native JSR310");
        dateLibrary.setEnum(dateOptions);
        cliOptions.add(dateLibrary);

        typeMapping.put("array", "kotlin.collections.List");
        typeMapping.put("list", "kotlin.collections.List");
        typeMapping.put("binary", "kotlin.collections.List<kotlin.Byte>");

        instantiationTypes.put("array", "listOf");
        instantiationTypes.put("list", "listOf");


        importMapping.put("StringListConverter", "com.mmm.thinbonding.Model.swagger.database.StringListConverter");
        importMapping.put("SoftDeletable", "com.mmm.thinbonding.Model.swagger.database.SoftDeletable");
        importMapping.put("UUIDEntity", "com.mmm.thinbonding.Model.swagger.database.UUIDEntity");
        importMapping.put("annotations", "io.objectbox.annotation.*");
        importMapping.put("ToMany", "io.objectbox.relation.ToMany");
        importMapping.put("ToOne", "io.objectbox.relation.ToOne");
        importMapping.put("SerializedName", "com.google.gson.annotations.SerializedName");
        

        CodegenModelFactory.setTypeMapping(CodegenModelType.PROPERTY, ObjectBoxProperty.class);
        CodegenModelFactory.setTypeMapping(CodegenModelType.MODEL, ObjectBoxModel.class);
    }

    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    public String getName() {
        return "kotlin-ObjectBox";
    }

    public String getHelp() {
        return "Generates a kotlin ObjectBox client.";
    }

    public void setDateLibrary(String library) {
        this.dateLibrary = library;
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(DATE_LIBRARY)) {
            setDateLibrary(additionalProperties.get(DATE_LIBRARY).toString());
        }

        if (DateLibrary.THREETENBP.value.equals(dateLibrary)) {
            additionalProperties.put(DateLibrary.THREETENBP.value, true);
            typeMapping.put("date", "LocalDate");
            typeMapping.put("DateTime", "LocalDateTime");
            importMapping.put("LocalDate", "org.threeten.bp.LocalDate");
            importMapping.put("LocalDateTime", "org.threeten.bp.LocalDateTime");
            defaultIncludes.add("org.threeten.bp.LocalDateTime");
        } else if (DateLibrary.STRING.value.equals(dateLibrary)) {
            typeMapping.put("date-time", "kotlin.String");
            typeMapping.put("date", "kotlin.String");
            typeMapping.put("Date", "kotlin.String");
            typeMapping.put("DateTime", "kotlin.String");
        } else if (DateLibrary.JAVA8.value.equals(dateLibrary)) {
            additionalProperties.put(DateLibrary.JAVA8.value, true);
        }

        supportingFiles.add(new SupportingFile("README.mustache", "", "README.md"));

        supportingFiles.add(new SupportingFile("build.gradle.mustache", "", "build.gradle"));
        supportingFiles.add(new SupportingFile("settings.gradle.mustache", "", "settings.gradle"));

        final String infrastructureFolder = (sourceFolder + File.separator + packageName + File.separator + "infrastructure").replace(".", "/");

        supportingFiles.add(new SupportingFile("infrastructure/ApiClient.kt.mustache", infrastructureFolder, "ApiClient.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/ApiAbstractions.kt.mustache", infrastructureFolder, "ApiAbstractions.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/ApiInfrastructureResponse.kt.mustache", infrastructureFolder, "ApiInfrastructureResponse.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/ApplicationDelegates.kt.mustache", infrastructureFolder, "ApplicationDelegates.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/RequestConfig.kt.mustache", infrastructureFolder, "RequestConfig.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/RequestMethod.kt.mustache", infrastructureFolder, "RequestMethod.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/ResponseExtensions.kt.mustache", infrastructureFolder, "ResponseExtensions.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/Serializer.kt.mustache", infrastructureFolder, "Serializer.kt"));
        supportingFiles.add(new SupportingFile("infrastructure/Errors.kt.mustache", infrastructureFolder, "Errors.kt"));
    }

    // overridden to post-process any model and property properties
    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property){

        if (property == null || !(property instanceof ObjectBoxProperty)) {
            return;
        }
        ObjectBoxProperty typedProperty = (ObjectBoxProperty) property;
        typedProperty.processCustomProperties();
    }

    @Override
    public void postProcessModel(CodegenModel model){
        if (model == null || !(model instanceof ObjectBoxModel)) {
            return;
        }
        ObjectBoxModel typedModel = (ObjectBoxModel) model;
        typedModel.processCustomProperties();
    }

    // override with any special post-processing
    @SuppressWarnings("static-method")
    @Override
    public Map<String, Object> postProcessAllModels(Map<String, Object> objs) {
        
        Map<String, Object> allModels = new HashMap<String, Object>();
        
        for (Entry<String, Object> entry : objs.entrySet()) {
            String modelName = toModelName(entry.getKey());
            Map<String, Object> inner = (Map<String, Object>) entry.getValue();
            List<Map<String, Object>> models = (List<Map<String, Object>>) inner.get("models");
            List<Map<String, Object>> newModels = new ArrayList<Map<String, Object>>();
            for (Map<String, Object> mo : models) {
                try {
                    CodegenModel cm = (CodegenModel) mo.get("model");
                    if(cm instanceof ObjectBoxModel && ((ObjectBoxModel) cm).isDatabaseModel) {
                        System.out.println("Found database model for model: " + cm.name);
                        newModels.add(mo);
                    }
                }catch(java.lang.NullPointerException e) {
                    e.printStackTrace();
                }
            }

            if(newModels.size() > 0) {
                inner.put("models", newModels);
                allModels.put(entry.getKey(), inner);
            }
        }
        return allModels;
    }

    /**
     * Return the capitalized file name of the model
     *
     * @param name the model name
     * @return the file name of the model
     */
    @Override
    public String toModelFilename(String name) {
        return DefaultCodegen.camelize(name) + "Entity";
    }

    static public class ObjectBoxProperty extends CodegenProperty {
        
        // Database Additions

        // Whether to index the property in the database
        public Boolean databaseIsIndexed; // x-database-is-indexed

        // Whether the attached object(s) is a reference to another table
        public Boolean databaseIsRelation; // x-database-is-relation

        // Whether the reference is a single uuid (false) or contains an array (true). If it contains an array, the reference will be a "to many" relationship.
        public Boolean databaseRelationIsToManyReference; // x-database-relation-is-to-many-reference
        public Boolean databaseRelationIsManyToManyReference; // x-database-relation-is-many-to-many-reference

        // The table/model name that the property relates to. For example "Category"
        public String databaseRelationModelType; // x-database-relation-model-type

        // The relation name for the referenced table (reference to Category object might be categories)
        public String databaseRelationPropertyName; // x-database-relation-property-name

        // The inverse relation for Core Data
        public String databaseRelationForeignPropertyName; // x-database-relation-foreign-property-name

        // Whether this table should link to a core data table. For example, we typically only want linking in one direction. For example, employees might load after stores, so during the employee setup, we link to stores, but when building stores, we don't try to link to employees because they haven't been loaded yet.
        public Boolean databaseRelationCreateLinkMethods; // x-database-relation-create-link-methods

        // This indicates that this property is only needed for database generation, and not necessary for API calls
        public Boolean databaseRelationOnlyProperty; // x-database-relation-only-property
        

        //Protocol Additions
        
        // This property keeps tracks of whether the object has been soft-deleted on the server.
        public Boolean isSoftDeletableProperty; // x-is-soft-deletable-property

        // This property keeps tracks of whether this is  a UUID property
        public Boolean isUUIDProperty; // x-is-uuid-property


        //Testing Additions

        //Ability to exclude something from auto-gen tests
        public Boolean isExcludedFromTests; //x-exclude-from-tests

        //Other

        public String nameInCamelCaseFirstLetterLower;

        public void processCustomProperties(){
            
            // Check JSON for custom code
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String,Object> jsonData = mapper.readValue(this.jsonSchema, Map.class);

                //Database 

                this.databaseIsIndexed = (Boolean) jsonData.get("x-database-is-indexed");
                this.databaseIsRelation = (Boolean) jsonData.get("x-database-is-relation");
                this.databaseRelationIsToManyReference = (Boolean) jsonData.get("x-database-relation-is-to-many-reference");
                this.databaseRelationIsManyToManyReference = (Boolean) jsonData.get("x-database-relation-is-many-to-many-reference");
                
                this.databaseRelationModelType = (String) jsonData.get("x-database-relation-model-type");
                this.databaseRelationPropertyName = (String) jsonData.get("x-database-relation-property-name");
                this.databaseRelationForeignPropertyName = (String) jsonData.get("x-database-relation-foreign-property-name");

                this.databaseRelationCreateLinkMethods = (Boolean) jsonData.get("x-database-relation-create-link-methods");
                this.databaseRelationOnlyProperty = (Boolean) jsonData.get("x-database-relation-only-property");
                
                //Protocols
                this.isSoftDeletableProperty = (Boolean) jsonData.get("x-is-soft-deletable-property");
                this.isUUIDProperty = (Boolean) jsonData.get("x-is-uuid-property");


                //Testing
                this.isExcludedFromTests = (Boolean) jsonData.get("x-exclude-from-tests");

                //Other
                this.nameInCamelCaseFirstLetterLower = DefaultCodegen.camelize(name, true);

            } catch (IOException e) {}
        }
    }
    
    static public class ObjectBoxModel extends CodegenModel {

        //Database Additions

        // Whether this model should be built during database generation
        public Boolean isDatabaseModel; // x-database-model
        public String databaseModelName; // x-database-model

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

        public void processCustomProperties() {
            
            // Check JSON for custom code
            try {
                ObjectMapper mapper = new ObjectMapper();
                Map<String,Object> jsonData = mapper.readValue(this.modelJson, Map.class);
                
                if(!jsonData.containsKey("x-database-model")) {
                    //In this case the model won't even be generated, so exit
                    return;
                }
                //Database
                this.isDatabaseModel = (Boolean) jsonData.get("x-database-model");
                this.databaseModelName = (String) jsonData.get("x-database-model-name");
                this.classFilename = this.databaseModelName;
                this.classname = this.databaseModelName;
                //Protocols
                this.isProtocolUUIDType = (Boolean) jsonData.get("x-protocol-uuid-type");
                this.isProtocolSortOrderType = (Boolean) jsonData.get("x-protocol-sort-order-type");
                this.isProtocolNameType = (Boolean) jsonData.get("x-protocol-name-type");
                this.isProtocolSoftDeletableType = (Boolean) jsonData.get("x-protocol-soft-deleteable-type");

                //Testing
                this.isExcludedFromTests = (Boolean) jsonData.get("x-exclude-from-tests");

                //Other
                this.isInitRequired = (Boolean) jsonData.get("x-init-required");
                
                if(this.isProtocolSoftDeletableType) {
                    imports.add("SoftDeletable");
                }
                if(this.isProtocolUUIDType) {
                    imports.add("UUIDEntity");
                }

                imports.add("StringListConverter");
                imports.add("annotations");
                imports.add("ToMany");
                imports.add("ToOne");
                imports.add("SerializedName");
    
            } catch (IOException e) {}
        }
    }
}
