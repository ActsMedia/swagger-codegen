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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class KotlinRetrofitCodegen extends AbstractKotlinCodegen {

    protected String invokerPackage = "io.swagger";

    public static final String DATE_LIBRARY = "dateLibrary";
    protected CodegenConstants.ENUM_PROPERTY_NAMING_TYPE enumPropertyNaming = CodegenConstants.ENUM_PROPERTY_NAMING_TYPE.camelCase;
    static Logger LOGGER = LoggerFactory.getLogger(KotlinRetrofitCodegen.class);

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
     * Constructs an instance of `KotlinRetrofitCodegen`.
     */
    public KotlinRetrofitCodegen() {
        super();

        artifactId = "kotlin-Retrofit";
        packageName = "com.mmm.mediaframework.Swagger";

        outputFolder = "generated-code" + File.separator + "kotlin-Retrofit";
        modelTemplateFiles.put("model.mustache", ".kt");
        modelDocTemplateFiles.put("model_doc.mustache", ".md");
        modelPackage = packageName + ".model";
        embeddedTemplateDir = templateDir = "kotlin-Retrofit";

        //We're using a single file for API generation, so we don't need to do this
        // apiDocTemplateFiles.put("api_doc.mustache", ".md");
        // apiTemplateFiles.put("api.mustache", ".kt");
        // apiPackage = packageName + ".apis";

        CliOption dateLibrary = new CliOption(DATE_LIBRARY, "Option. Date library to use");
        Map<String, String> dateOptions = new HashMap<>();
        dateOptions.put(DateLibrary.THREETENBP.value, "Threetenbp");
        dateOptions.put(DateLibrary.STRING.value, "String");
        dateOptions.put(DateLibrary.JAVA8.value, "Java 8 native JSR310");
        dateLibrary.setEnum(dateOptions);
        cliOptions.add(dateLibrary);

        CodegenModelFactory.setTypeMapping(CodegenModelType.PROPERTY, DatabaseCodegenProperty.class);
        CodegenModelFactory.setTypeMapping(CodegenModelType.MODEL, DatabaseCodegenModel.class);
    }

    public CodegenType getTag() {
        return CodegenType.CLIENT;
    }

    public String getName() {
        return "kotlin-Retrofit";
    }

    public String getHelp() {
        return "Generates a kotlin client.";
    }

    public void setDateLibrary(String library) {
        this.dateLibrary = library;
    }

    @Override
    public void processOpts() {
        super.processOpts();

        if (additionalProperties.containsKey(CodegenConstants.INVOKER_PACKAGE)) {
            this.invokerPackage = (String) additionalProperties.get(CodegenConstants.INVOKER_PACKAGE);
            additionalProperties.put(CodegenConstants.INVOKER_PACKAGE, invokerPackage);
            packageName = invokerPackage + ".swagger";
            additionalProperties.put(CodegenConstants.PACKAGE_NAME, packageName);
            additionalProperties.put(CodegenConstants.MODEL_PACKAGE, packageName + ".models");
            modelPackage = packageName + ".models";
            additionalProperties.put(CodegenConstants.API_PACKAGE, packageName + ".api");
            apiPackage = packageName + ".api";
        } 

        if (!additionalProperties.containsKey(CodegenConstants.MODEL_PACKAGE)) {
            additionalProperties.put(CodegenConstants.MODEL_PACKAGE, modelPackage);
        }

        if (!additionalProperties.containsKey(CodegenConstants.API_PACKAGE)) {
            additionalProperties.put(CodegenConstants.API_PACKAGE, apiPackage);
        }

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

        final String infrastructureFolder = (sourceFolder + File.separator + packageName + File.separator + "infrastructure").replace(".", "/");
        final String apiFolder = (sourceFolder + File.separator + apiPackage).replace(".", "/");

        supportingFiles.add(new SupportingFile("retrofit_api.mustache", apiFolder, "RetrofitAPI.kt"));
    }

    // overridden to post-process any property properties
    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property){

        if (property == null || !(property instanceof DatabaseCodegenProperty)) {
            return;
        }
        DatabaseCodegenProperty typedProperty = (DatabaseCodegenProperty) property;
        typedProperty.processCustomProperties();
    }

    //Overridden to post-process any model properties.
    @Override
    public void postProcessModel(CodegenModel model){
        if (model == null || !(model instanceof DatabaseCodegenModel)) {
            return;
        }
        DatabaseCodegenModel typedModel = (DatabaseCodegenModel) model;
        typedModel.processCustomProperties(false);
    }
}