package io.swagger.codegen.languages;

import io.swagger.codegen.CodegenModelFactory;
import io.swagger.codegen.CodegenModelType;
import io.swagger.codegen.CodegenParameter;
import io.swagger.codegen.CodegenModel;
import io.swagger.codegen.CodegenProperty;
import io.swagger.codegen.CliOption;
import io.swagger.codegen.CodegenConstants;
import io.swagger.codegen.CodegenType;
import io.swagger.codegen.SupportingFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;

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
        apiPackage = packageName + ".apis";
        modelPackage = packageName + ".models";

        CliOption dateLibrary = new CliOption(DATE_LIBRARY, "Option. Date library to use");
        Map<String, String> dateOptions = new HashMap<>();
        dateOptions.put(DateLibrary.THREETENBP.value, "Threetenbp");
        dateOptions.put(DateLibrary.STRING.value, "String");
        dateOptions.put(DateLibrary.JAVA8.value, "Java 8 native JSR310");
        dateLibrary.setEnum(dateOptions);
        cliOptions.add(dateLibrary);

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

        // Check JSON for custom code
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> jsonData = mapper.readValue(property.jsonSchema, Map.class);

            typedProperty.isIndexed = (Boolean) jsonData.get("x-is-indexed");
            typedProperty.isForeignTableReferenceByUUID = (Boolean) jsonData.get("x-is-foreign-table-reference-by-uuid");
            typedProperty.isToManyReference = (Boolean) jsonData.get("x-is-to-many-reference");
            typedProperty.isCreateTableLinkMethods = (Boolean) jsonData.get("x-is-enable-table-link-methods");
            typedProperty.referencesPropertyName = (String) jsonData.get("x-references-property-name");
            typedProperty.referencesRelationName = (String) jsonData.get("x-references-relation-name");
            typedProperty.referenceInverseName = (String) jsonData.get("x-reference-inverse-name");
            typedProperty.isDeletedOnServerProperty = (Boolean) jsonData.get("x-deleted-on-server-property");
            typedProperty.isExcludedFromTests = (Boolean) jsonData.get("x-exclude-from-tests");

        } catch (IOException e) {}
    }

    @Override
    public void postProcessModel(CodegenModel model){
        if (model == null || !(model instanceof ObjectBoxModel)) {
            return;
        }
        ObjectBoxModel typedModel = (ObjectBoxModel) model;
        
        // Check JSON for custom code
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String,Object> jsonData = mapper.readValue(model.modelJson, Map.class);
            
			typedModel.isExcludedFromTests = (Boolean) jsonData.get("x-exclude-from-tests");
            typedModel.isInitRequired = (Boolean) jsonData.get("x-init-required");
            typedModel.isBuildCoreData = (Boolean) jsonData.get("x-build-core-data");
            typedModel.isProtocolUUIDType = (Boolean) jsonData.get("x-protocol-uuid-type");
            typedModel.isProtocolSortOrderType = (Boolean) jsonData.get("x-protocol-sort-order-type");
            typedModel.isProtocolNameType = (Boolean) jsonData.get("x-protocol-name-type");
            typedModel.isProtocolSoftDeletableType = (Boolean) jsonData.get("x-protocol-soft-deleteable-type");

        } catch (IOException e) {}
    }

    static public class ObjectBoxProperty extends CodegenProperty {
        // DWS Additions

        // Whether to index the property in the database
        public Boolean isIndexed; // x-is-indexed

        // Whether the attached object(s) is a reference to another table
        public Boolean isForeignTableReferenceByUUID; // x-is-foreign-table-reference-by-uuid

        // Whether this table should link to a core data table. For example, we typically only want linking in one direction. For example, employees might load after stores, so during the employee setup, we link to stores, but when building stores, we don't try to link to employees because they haven't been loaded yet.
        public Boolean isCreateTableLinkMethods; // x-is-enable-table-link-methods

        // Whether the reference is a single uuid (false) or contains an array (true). If it contains an array, the reference will be a "to many" relationship.
        public Boolean isToManyReference; // x-is-to-many-reference

        // The table/model name that the property relates to. For example "Category"
        public String referencesPropertyName; // x-references-property-name

        // The relation name for the referenced table (reference to Category object might be categories)
        public String referencesRelationName; // x-references-relation-name

        // The inverse relation for Core Data
        public String referenceInverseName; // x-reference-inverse-name

        // This property keeps tracks of whether the object has been soft-deleted on the server.
        public Boolean isDeletedOnServerProperty; // x-is-deleted-on-server-property

        // PJF Additions
        //Ability to exclude something from auto-gen tests
        public Boolean isExcludedFromTests; //x-exclude-from-tests
    }
    
    static public class ObjectBoxModel extends CodegenModel {

        // DWS Additions

        // Builds a convenience initializer with required variables
        public Boolean isInitRequired; // x-init-required

        // Whether to automatically build the corresponding core-data model
        // Requires that each model object conforms to UuidFindable
        public Boolean isBuildCoreData; // x-build-core-data

        // Protocols
        public Boolean isProtocolUUIDType; // x-protocol-uuid-type
        public Boolean isProtocolSortOrderType; // x-protocol-sort-order-type
        public Boolean isProtocolNameType; // x-protocol-name-type
        public Boolean isProtocolSoftDeletableType; // x-protocol-soft-deleteable-type

        public Boolean isExcludedFromTests; //x-exclude-from-tests

    }
}
