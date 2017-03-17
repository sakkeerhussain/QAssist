package com.qburst.plugin.android.retrofit;
import com.android.tools.idea.gradle.parser.BuildFileKey;
import com.android.tools.idea.gradle.parser.Dependency;
import com.android.tools.idea.gradle.parser.GradleBuildFile;
import com.android.tools.idea.gradle.parser.GradleSettingsFile;
import com.android.tools.idea.gradle.project.GradleProjectImporter;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.SourceFolder;
import com.intellij.openapi.roots.impl.SourceFolderImpl;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import com.intellij.util.SmartList;
import com.qburst.plugin.android.retrofit.forms.Form1;
import com.qburst.plugin.android.retrofit.forms.Form2;
import com.qburst.plugin.android.retrofit.forms.Form3;
import com.qburst.plugin.android.utils.classutils.*;
import com.qburst.plugin.android.utils.http.HTTPUtils;
import com.qburst.plugin.android.utils.http.UrlParamModel;
import com.qburst.plugin.android.utils.log.Log;
import com.qburst.plugin.android.utils.notification.NotificationManager;
import com.qburst.plugin.android.utils.string.ClassStringUtil;
import com.qburst.plugin.android.utils.string.Const;
import com.qburst.plugin.android.utils.string.StringUtils;
import com.qburst.plugin.android.utils.string.UrlStringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;
import org.jetbrains.jps.model.java.JavaSourceRootProperties;
import org.jetbrains.jps.model.java.JavaSourceRootType;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;
import javax.swing.*;
import java.util.*;

/**
 * Created by sakkeer on 11/01/17.
 */
public class RetrofitController {
    private static final String TAG = "RetrofitController";
    public boolean isRepairMode() {
        return repairMode;
    }
    private boolean repairMode;
    private Project project;
    private Module moduleSelected;
    private String baseUrl;
    private String packageName;
    private int noOfEndPoints;
    private List<EndPointDataModel> endPointDataModelList;
    private boolean gradleFileChanged;
    public RetrofitController() {
        endPointDataModelList = new ArrayList<>();
        packageName = Constants.PACKAGE_NAME_RETROFIT;
        repairMode = false;
    }

    private JFrame frame;

    private SourceFolder sourceFolderSelected;

    public void integrateRetrofitAction(AnActionEvent event) {
        this.project = event.getProject();
        this.frame = new JFrame("Retrofit");
        openForm1();
    }
    public int getSizeofEndPointDataModelList()
    {
        return endPointDataModelList.size();
    }
    public void  setSizeofEndPointDataModelList(int size)
    {
        for(int i = noOfEndPoints; i<endPointDataModelList.size();i++)
        {
            endPointDataModelList.remove(i);
        }
        return ;
    }

    public boolean isAvailRepairRetrofitAction(AnActionEvent event) {
        return !(ClassManager.get().isClassExists("RetrofitManager", event.getProject(), this) == null
                && ClassManager.get().isClassExists("APIService", event.getProject(), this) == null);
    }

    public void repairRetrofitAction(AnActionEvent event) {
        repairMode = true;
        project = event.getProject();
        PsiClass managerClass = ClassManager.get().isClassExists(Constants.ClassName.MANAGER, project, this).getClasses()[0];
        PsiClass serviceClass = ClassManager.get().isClassExists(Constants.ClassName.SERVICE, project, this).getClasses()[0];
        readDataFromPreExistingClasses(managerClass, serviceClass);
        integrateRetrofitAction(event);
    }

    private void readDataFromPreExistingClasses(PsiClass managerClass, PsiClass serviceClass) {
        moduleSelected = ClassManager.get().getContainingModule(managerClass);
        packageName = managerClass.getQualifiedName().replace("."+managerClass.getName(), "");
        baseUrl = (String) ClassManager.get().getFieldValue(Constants.ManagerClass.FIELD_BASE_URL, managerClass);
        if (baseUrl == null) baseUrl = "";
        PsiMethod[] methods = serviceClass.getMethods();
        noOfEndPoints = methods.length;
        for(int i = 0; i < noOfEndPoints; i++){
            PsiMethod method = methods[i];
            EndPointDataModel endPointDataModel = new EndPointDataModel();
            endPointDataModel.setEndPointNo(i+1);
            endPointDataModel.setCreateIgnoreModelClasses(true);
            endPointDataModel.setEndPointName(method.getName());
            // TODO: 02/03/17 handle no annotation case
            PsiAnnotation annotation = method.getModifierList().getAnnotations()[0];
            String httpMethod = annotation.getQualifiedName();
            httpMethod = ClassStringUtil.getClassNameFromQualified(httpMethod);
            endPointDataModel.setMethod(httpMethod);
            String endPointUrl = StringUtils.getUnwrapStringValue(annotation.getParameterList().getAttributes()[0].getValue().getText());
            endPointUrl = UrlStringUtil.getUrlWithDummyData(endPointUrl, method.getParameterList());
            endPointDataModel.setEndPointUrl(endPointUrl);
            PsiType responseClassType = ((PsiClassReferenceType) method.getReturnType()).getParameters()[0];
            endPointDataModel.setResponseModel(JsonManager.getJsonFromPsiClass(responseClassType));
            endPointDataModel.setResponseModelClassName(responseClassType.getCanonicalText());
            for (PsiParameter psiParameter: method.getParameterList().getParameters()){
                Log.d("PsiParameter", psiParameter.getText());
                PsiAnnotation psiParameterAnnotation = psiParameter.getModifierList().getAnnotations()[0];
                String paramType = psiParameterAnnotation.getQualifiedName();
                if (Const.Retrofit.BODY.equals(paramType)) {
                    PsiType requestClassType = psiParameter.getType();
                    endPointDataModel.setRequestModel(JsonManager.getJsonFromPsiClass(requestClassType));
                    endPointDataModel.setRequestModelClassName(requestClassType.getCanonicalText());
                }
            }
            endPointDataModelList.add(endPointDataModel);
        }
    }

    public void openForm1(){
        Log.d(TAG, "openForm1() called");
        String[] flags = new String[0];
        Form1 form1 = Form1.main(flags, frame);
        form1.setData(this, project, baseUrl, packageName, noOfEndPoints, moduleSelected);
    }

    public void openForm2(boolean fromStart){
        Log.d(TAG, "openForm2() called");
        String[] flags = new String[0];
        Form2 form = Form2.main(flags, frame);
        form.setData(this);
        if (fromStart) {
            form.setCurrentEndPoint(1);
        }else{
            form.setCurrentEndPoint(this.noOfEndPoints);
        }
    }

    public void openForm3(){
        Log.d(TAG, "openForm3() called");
        String[] flags = new String[0];
        Form3 form3 = Form3.main(flags, frame);
        form3.setData(this);
    }

    public void hideForm(){
        Log.d(TAG, "hideForm() called");
        frame.setVisible(false);
    }

    public void setTitle(String title){
        frame.setTitle("Retrofit - "+title);
    }

    public void setModuleSelected(Module moduleSelected) {
        this.moduleSelected = moduleSelected;
    }
    public void setSourceFolderSelected(SourceFolder sourceFolderSelected){
        this.sourceFolderSelected = sourceFolderSelected;
    }

    public Module getModuleSelected(){
        return moduleSelected;
    }

    public void setEndPointDataModel(EndPointDataModel endPointDataModel) {
        boolean alreadyExisting = false;
        for (EndPointDataModel endPointDataModelIter : endPointDataModelList) {
            if (endPointDataModelIter.getEndPointNo() == endPointDataModel.getEndPointNo()) {
                alreadyExisting = true;
                this.endPointDataModelList.set(this.endPointDataModelList.indexOf(endPointDataModelIter), endPointDataModel);
            }
        }
        if (!alreadyExisting) {
            this.endPointDataModelList.add(endPointDataModel);
        }
    }

    public EndPointDataModel getEndPointDataModel(int position) {
        for (EndPointDataModel endPointDataModel : endPointDataModelList) {
            if (endPointDataModel.getEndPointNo() == position) {
                return endPointDataModel;
            }
        }

        return new EndPointDataModel();
    }

    public int getNoOfEndPoints() {
        return noOfEndPoints;
    }

    public void integrateRetrofit() {
        hideForm();
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Integrating Retrofit") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setIndeterminate(false);
                indicator.setFraction(0);
                WriteCommandAction.runWriteCommandAction(project, () -> {
                    String errorMessage = null;
                    if (!addDependencies(indicator)) {
                        errorMessage = "Dependency injection failed!";
                    } else if (!createPackage(indicator)) {
                        errorMessage = "Creating package failed!";
                    } else if (!createClasses(indicator)) {
                        errorMessage = "Creating Class failed!";
                    }

                    if (errorMessage == null) {
                        NotificationManager.get().integrationCompletedNotification(project);
                        if (gradleFileChanged){
                            ApplicationManager.getApplication().invokeLater(() -> {
                                GradleProjectImporter.getInstance().requestProjectSync(project, null);
                            });
                        }
                    } else {
                        NotificationManager.get().integrationFailedNotification(project, errorMessage);
                    }
                });
            }
        });
    }

    private boolean addDependencies(ProgressIndicator indicator) {
        indicator.setText("Adding dependencies");
        if (moduleSelected == null) { return false; }
        String moduleGradlePath = GradleSettingsFile.getModuleGradlePath(moduleSelected);
        if (moduleGradlePath == null) { return false; }
        GradleSettingsFile mySettingsFile = GradleSettingsFile.get(project);
        final GradleBuildFile buildFile;
        if (mySettingsFile == null) { return false; }
        buildFile = mySettingsFile.getModuleBuildFile(moduleGradlePath);
        if (buildFile == null) { return false; }
        List<Dependency> value = (List<Dependency>)buildFile.getValue(BuildFileKey.DEPENDENCIES);
        final List<Dependency> dependencies = value != null ? value : new ArrayList<Dependency>();
        gradleFileChanged = false;
        //new MavenArtifact
        Dependency retrofit = new Dependency(Dependency.Scope.COMPILE,
                Dependency.Type.EXTERNAL,
                Constants.DEPENDENCY_RETROFIT);
        if (!dependencies.contains(retrofit)) {
            dependencies.add((retrofit));
            gradleFileChanged = true;
        }
        Dependency retrofitGson = new Dependency(Dependency.Scope.COMPILE,
                Dependency.Type.EXTERNAL,
                Constants.DEPENDENCY_RETROFIT_GSON);
        if (!dependencies.contains(retrofitGson)) {
            dependencies.add((retrofitGson));
            gradleFileChanged = true;
        }
        Dependency retrofitLogging = new Dependency(Dependency.Scope.COMPILE,
                Dependency.Type.EXTERNAL,
                Constants.DEPENDENCY_RETROFIT_LOGGING);
        if (!dependencies.contains(retrofitLogging)) {
            dependencies.add((retrofitLogging));
            gradleFileChanged = true;
        }
        if (gradleFileChanged) {
            buildFile.setValue(BuildFileKey.DEPENDENCIES, dependencies);
        }
        return true;
    }

    private boolean createPackage(ProgressIndicator indicator){
        indicator.setText("Creating directories");
        indicator.setFraction(0.2);
        DirectoryManager directoryManager = DirectoryManager.get();

        String[] dirNames = packageName.split("\\.");
        VirtualFile dir = sourceFolderSelected.getFile();
        for (String dirName : dirNames) {
            dir = directoryManager.createDirectory(dir, dirName);
            if (dir == null){ return false; }
        }
        VirtualFile modelDir = directoryManager.createDirectory(dir, "model");
        if (modelDir == null){ return false; }
        if (directoryManager.createDirectory(modelDir, "request") == null){ return false; }
        return directoryManager.createDirectory(modelDir, "response") != null;
    }

    private boolean createClasses(ProgressIndicator indicator) {
        PsiPackage pkgRequest = JavaPsiFacade.getInstance(project).findPackage(packageName + Constants.PACKAGE_NAME_RETROFIT_REQUEST);
        PsiPackage pkgResponse = JavaPsiFacade.getInstance(project).findPackage(packageName + Constants.PACKAGE_NAME_RETROFIT_RESPONSE);
        if (pkgRequest == null || pkgResponse == null) {
            //NotificationManager.get().integrationFailedNotification(project, errorMessage);
            return false;
        }
        indicator.setText("Creating request model classes");
        indicator.setFraction(0.4);
        if (!createRequestModelClasses(pkgRequest.getDirectories()[0], indicator)) {
            return false;
        }
        indicator.setText("Creating response model classes");
        indicator.setFraction(0.6);
        if (!createResponseModelClasses(pkgResponse.getDirectories()[0], indicator)) {
            return false;
        }
        //get retrofit directory
        PsiPackage pkg = JavaPsiFacade.getInstance(project).findPackage(packageName);
        if (pkg == null) {
            return false;
        }
        PsiDirectory psiDirectory = pkg.getDirectories()[0];
        return createServiceClass(psiDirectory, indicator)
                && createManagerClass(psiDirectory, indicator);
    }
    private boolean createRequestModelClasses(PsiDirectory psiDirectoryRequest, ProgressIndicator indicator) {
        for (EndPointDataModel endPointDataModel : endPointDataModelList) {
            if (HTTPUtils.isPayloadNotSupportingMethod(endPointDataModel.getMethod())) {
                continue;
            }
            ClassModel classModel = new JsonManager().getRequestClassModel(endPointDataModel,
                    project, psiDirectoryRequest);
            indicator.setText("Creating "+classModel.getName());
            classModel.setPackageName(packageName  + Constants.PACKAGE_NAME_RETROFIT_REQUEST);
            endPointDataModel.setRequestModelClassName(classModel.getQualifiedName());
            generateGetterAndSetterMethod(classModel);
            if (!ClassManager.get().createClass(classModel)){
                return false;
            }
        }
        return true;
    }

    private void generateGetterAndSetterMethod(ClassModel classModel) {
        List<FieldModel> fieldModels= classModel.getFields();
        if(classModel.getSubClasses().size()>0)
        {
            for(int i =0;i<classModel.getSubClasses().size();i++){
                generateGetterAndSetterMethod(classModel.getSubClasses().get(i));
            }
        }
        for (FieldModel field : fieldModels) {
            String fieldName = field.getFieldName();
            String fieldType = field.getFullNameType();
            String getMethodName = "get" + StringUtils.capitaliseFirstLetter(field.getFieldName());
            String setMethodName = "set" + StringUtils.capitaliseFirstLetter(field.getFieldName());
            String getInnerContent = "return this." + fieldName + ";";
            String setInnerContent = "this." + fieldName + " = " + fieldName + ";";
            List<ParameterModel> parameterModels = new ArrayList<>();
            ParameterModel parameterModel = new ParameterModel(fieldType, fieldName);
            parameterModels.add(parameterModel);
            MethodModel getMethod = new MethodModel(classModel, "public", false, fieldType, getMethodName, null, getInnerContent);
            MethodModel setMethod = new MethodModel(classModel, "public", false, "void", setMethodName, parameterModels, setInnerContent);
            classModel.addMethod(getMethod);
            classModel.addMethod(setMethod);
        }
    }

    private boolean createResponseModelClasses(PsiDirectory psiDirectoryResponse, ProgressIndicator indicator) {
        // TODO: 10/02/17 Create base class for response model accoding to comon fields of all APIs.
        for (EndPointDataModel endPointDataModel : endPointDataModelList) {
            ClassModel classModel = new JsonManager().getResponseClassModel(endPointDataModel,
                    project, psiDirectoryResponse);
            classModel.setPackageName(packageName  + Constants.PACKAGE_NAME_RETROFIT_RESPONSE);
            endPointDataModel.setResponseModelClassName(classModel.getQualifiedName());
            indicator.setText("Creating "+classModel.getName());
            generateGetterAndSetterMethod(classModel);
            if (!ClassManager.get().createClass(classModel)){
                return false;
            }
        }
        return true;
    }

    private boolean createServiceClass(PsiDirectory psiDirectory, ProgressIndicator indicator) {

        //Creating service class
        indicator.setText("Creating service class");
        indicator.setFraction(0.8);
        ClassModel classModel = new ClassModel(project, psiDirectory, Constants.ClassName.SERVICE, ClassModel.Type.INTERFACE);
        classModel.setPackageName(packageName);
        for (int i = 0; i < noOfEndPoints; i++) {
            EndPointDataModel endPointData = endPointDataModelList.get(i);

            String url = endPointData.getEndPointUrl();
            String prettyUrl = UrlStringUtil.getPrettyUrl(url);

            String annotationString = String.format(Constants.ServiceInterface.ANNOTATION_FORMAT,
                    endPointData.getMethod(), prettyUrl);

            String requestParamsString = "";
            String requestClassName = endPointData.getRequestModelClassName();
            if (requestClassName != null && !requestClassName.equals("")) {
                String requestClassObjName =  ClassStringUtil.getClassNameFromQualified(requestClassName);
                requestClassObjName = StringUtils.lowersFirstLetter(requestClassObjName);
                String reqBodyStr = String.format(Constants.ServiceInterface.REQUEST_PARAM_BODY,
                        requestClassName,
                        requestClassObjName);
                requestParamsString = requestParamsString.concat(reqBodyStr);
            }

            List<UrlParamModel> pathParams = UrlStringUtil.getListOfPathParams(url);
            for (UrlParamModel pathParam:pathParams){
                String reqQueryStr = String.format(Constants.ServiceInterface.REQUEST_PARAM_PATH,
                        pathParam.getKey(), new UrlStringUtil().getParamType(pathParam.getValue()),
                        StringUtils.lowersFirstLetter(pathParam.getKey()));
                requestParamsString = requestParamsString.concat(reqQueryStr);
            }

            List<UrlParamModel> queryParams = UrlStringUtil.getListOfQueryParams(url);
            for (UrlParamModel queryParam:queryParams){
                String reqQueryStr = String.format(Constants.ServiceInterface.REQUEST_PARAM_QUERY,
                        queryParam.getKey(), new UrlStringUtil().getParamType(queryParam.getValue()),
                        StringUtils.lowersFirstLetter(queryParam.getKey()));
                requestParamsString = requestParamsString.concat(reqQueryStr);
            }
            if (requestParamsString.length() > 1
                    && requestParamsString.charAt(requestParamsString.length() - 1) == ' '
                    && requestParamsString.charAt(requestParamsString.length() - 2) == ',') {
                requestParamsString = requestParamsString.substring(0, requestParamsString.length()-2);
            }
            String methodString = String.format(Constants.ServiceInterface.METHOD,
                    annotationString,
                    endPointData.getResponseModelClassName(),
                    endPointData.getEndPointName(),
                    requestParamsString);

            classModel.addMethod(methodString);
        }

        return ClassManager.get().createClass(classModel);
    }

    private boolean createManagerClass(PsiDirectory psiDirectory, ProgressIndicator indicator) {
        indicator.setText("Creating manager class");
        indicator.setFraction(1.0);
        ClassModel classModel = new ClassModel(project, psiDirectory, Constants.ClassName.MANAGER, ClassModel.Type.CLASS);
        classModel.setPackageName(packageName);
        FieldModel staticField = new FieldModel(classModel, "private", true, true,
                "String", "BASE_URL");
        staticField.setValue(StringUtils.getValueAsString(baseUrl));
        classModel.addField(staticField);
        classModel.addMethod(Constants.GET_INSTANCE_METHOD);
        return ClassManager.get().createClass(classModel);
    }

    public List<SourceFolder> getSourceRoots(Module moduleSelected) {
        List<SourceFolder> result = new SmartList<>();
        ModuleRootManager root = ModuleRootManager.getInstance(moduleSelected);
        for (ContentEntry contentEntry : root.getContentEntries()) {
            Set<? extends JpsModuleSourceRootType<?>> rootType = Collections.singleton(JavaSourceRootType.SOURCE);
            final List<SourceFolder> sourceFolders = contentEntry.getSourceFolders(rootType);
            for (SourceFolder sourceFolder : sourceFolders) {
                if (sourceFolder == null) continue;
                if (sourceFolder.getFile() == null) continue;
                if (isForGeneratedSources((SourceFolderImpl) sourceFolder)) continue;
                result.add(sourceFolder);
            }
        }
        return result;
    }

    private boolean isForGeneratedSources(SourceFolderImpl sourceFolder) {
        JavaSourceRootProperties properties = sourceFolder.getJpsElement().getProperties(JavaModuleSourceRootTypes.SOURCES);
        return properties != null && properties.isForGeneratedSources();
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public void setNoOfEndPoints(int noOfEndPoints) {
        this.noOfEndPoints = noOfEndPoints;
    }

}
