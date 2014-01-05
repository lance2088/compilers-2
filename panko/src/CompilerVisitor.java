import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.stringtemplate.v4.*;

public class CompilerVisitor extends pankoBaseVisitor<CodeFragment> {
        private Map<String, String> mem = new HashMap<String, String>();
        private int labelIndex = 0;
        private int registerIndex = 0;
        
        private int functionIndex = 0;
        private Map<String, FunctionFragment> functions = new HashMap<String, FunctionFragment>();
        private boolean inFunctionDefine = false; 
        private String inFunctionName = ""; 
        private final String FUNCTION_MAIN_NAME = "_main"; //so it cannot collide with any function name defined by user 
        
    	//global variables which were shadowed by function variables 
    	Map<String, String> paramsShadow = new HashMap<String, String>(); 
    	List<String> registeredLocalVariables = new ArrayList<String>();
        List<String> globalVariableDeclarations = new ArrayList<String>(); 

        private String generateNewLabel() {
                return String.format("L%d", this.labelIndex++);
        }
        
        private String generateNewRegister() {
        	return this.generateNewRegister(false, null); 
        }

        private String generateNewRegister(boolean global, String definition) {
        	if(global){
        		String result = String.format("@R%d", this.registerIndex++);
        		this.globalVariableDeclarations.add(result + " = common global " + definition); 
        		return result;
        	}
        	else{
        		return String.format("%%R%d", this.registerIndex++);
        	}
        }

        //TODO StringBuilder
        private String getGlobalDefinitions() {
        	String result = "";
        	for(String line : this.globalVariableDeclarations){
        		result += line + "\n"; 
        	}
        	return result; 
        }

        //TODO StringBuilder
        private String generateNewFunction() {
            return String.format("@f%d", this.functionIndex++);
        }

        private String getFunctionDefinitions() {
        	String result = "";
        	for(Map.Entry<String, FunctionFragment> entry : functions.entrySet()){
        		if(!entry.getKey().equals(FUNCTION_MAIN_NAME)){
        			result += entry.getValue().all_code.toString();
        		}
        	}
        	return result; 
        }
        
        @Override
        public CodeFragment visitInit(pankoParser.InitContext ctx) {
                CodeFragment body = visit(ctx.statements());

                ST template = new ST(
                        "declare i32 @printInt(i32)\n" + 
                        "declare i32 @printChar(i8)\n" + 
                        "declare i32 @printFloat(float)\n" +
                        "declare i32 @scanInt()\n" + 
                        "declare i8 @scanChar()\n" + 
                        "declare float @scanFloat()\n" + 
                        "declare i32 @iexp(i32, i32)\n" + //TODO should be native 
                        "declare i32* @MALLOC(i32)\n" +  
                        "declare i32 @FREE(i32*)\n" + 
                        "declare i32 @SET_RANDOM()\n" +  
                        "declare i32 @RANDOM(i32)\n" +   
                        "<global_definitions>\n" + 
                        "<function_definitions>\n" + 
                        "define i32 @main() {\n" + 
                        "start:\n" + 
                        "call i32 @SET_RANDOM()\n" + 
                        "<global_code>\n" + 
                        "<main_code>" +
                        "}\n"
                );
                template.add("global_definitions", this.getGlobalDefinitions());
                template.add("function_definitions", this.getFunctionDefinitions());
                
                //TODO is there global context in LLVM? 
                if(this.functions.containsKey(FUNCTION_MAIN_NAME)){
                	template.add("global_code", body); 
                	template.add("main_code", this.functions.get(FUNCTION_MAIN_NAME).body_code.toString());
                }
                else{
                	template.add("global_code", body); 
                	template.add("main_code", "ret i32 0\n");
                }

                CodeFragment code = new CodeFragment();
                code.addCode(template.render());
                code.setRegister(body.getRegister());
                return code;
        }
        
        @Override
        public CodeFragment visitStatements(pankoParser.StatementsContext ctx) {
                CodeFragment code = new CodeFragment();
                for(pankoParser.StatementContext s: ctx.statement()) {
                        CodeFragment statement = visit(s);
                        code.addCode(statement);
                        code.setRegister(statement.getRegister());
                }
                return code;
        }
        
        @Override
    	public CodeFragment visitRValue(@NotNull pankoParser.RValueContext ctx){
            return visit(ctx.rvalue());
    	}

        @Override
    	public CodeFragment visitMod(@NotNull pankoParser.ModContext ctx){
            return generateBinaryOperator(
                    visit(ctx.rvalue(0)),
                    visit(ctx.rvalue(1)),
                    ctx.op.getType()
            );
    	}

        @Override
    	public CodeFragment visitExp(@NotNull pankoParser.ExpContext ctx){
            return generateBinaryOperator(
                    visit(ctx.rvalue(0)),
                    visit(ctx.rvalue(1)),
                    ctx.op.getType()
            );
    	}

        @Override
    	public CodeFragment visitMul(@NotNull pankoParser.MulContext ctx){
            return generateBinaryOperator(
                    visit(ctx.rvalue(0)),
                    visit(ctx.rvalue(1)),
                    ctx.op.getType()
            );
    	}
        
        @Override
    	public CodeFragment visitAdd(@NotNull pankoParser.AddContext ctx){
            return generateBinaryOperator(
                    visit(ctx.rvalue(0)),
                    visit(ctx.rvalue(1)),
                    ctx.op.getType()
            );
    	}
        

        @Override 
        public CodeFragment visitNot(pankoParser.NotContext ctx) {
                return generateUnaryOperator(
                        visit(ctx.rvalue()),
                        ctx.op.getType()
                );
        }

        @Override
        public CodeFragment visitAnd(pankoParser.AndContext ctx) {
                return generateBinaryOperator(
                        visit(ctx.rvalue(0)),
                        visit(ctx.rvalue(1)),
                        ctx.op.getType()
                );
        }

        @Override
        public CodeFragment visitOr(pankoParser.OrContext ctx) {
                return generateBinaryOperator(
                        visit(ctx.rvalue(0)),
                        visit(ctx.rvalue(1)),
                        ctx.op.getType()
                );
        }
        
        //TODO why
        @Override
        public CodeFragment visitEqual(pankoParser.EqualContext ctx) {
                return generateBinaryOperator(
                        visit(ctx.rvalue(0)),
                        visit(ctx.rvalue(1)),
                        ctx.op.getType()
                );
        }
        
        @Override
        public CodeFragment visitSmaller(pankoParser.SmallerContext ctx) {
                return generateBinaryOperator(
                        visit(ctx.rvalue(0)),
                        visit(ctx.rvalue(1)),
                        ctx.op.getType()
                );
        }
        
        @Override
    	public CodeFragment visitPipkos(@NotNull pankoParser.PipkosContext ctx){
        	return generateConstant("0"); 
        }
        
        @Override
    	public CodeFragment visitFajne(@NotNull pankoParser.FajneContext ctx){
        	return generateConstant("1"); 
        }

        @Override
    	public CodeFragment visitTisic(@NotNull pankoParser.TisicContext ctx){
            return generateConstant("2147483647"); 
    	}

        @Override
        public CodeFragment visitInt(pankoParser.IntContext ctx) {
        	return generateConstant(ctx.INT().getText()); 
        }

        //     | BAVI rvalue                                # RandomValue
        @Override
        public CodeFragment visitRandomValue(pankoParser.RandomValueContext ctx) {
        	CodeFragment valueCode = visit(ctx.rvalue()); 
        	
    		String ret_reg = this.generateNewRegister(); 
    		
            ST template = new ST(
            		"<value_code>" + 
                    "<ret_reg> = call i32 @RANDOM(i32 <value_reg>)\n"
            );
            template.add("value_code", valueCode.toString());
            template.add("value_reg", valueCode.getRegister());
            template.add("ret_reg", ret_reg);
            
            return new CodeFragment(template.render(), ret_reg);
        }
        
        @Override
    	public CodeFragment visitSuchy(@NotNull pankoParser.SuchyContext ctx){
            return new CodeFragment();
    	}
        
        @Override
    	public CodeFragment visitEmp(@NotNull pankoParser.EmpContext ctx){
            return new CodeFragment();
    	}

        //TODO refactor with visitAssign
        //TODO type 
        //     | VMOTAJ TYPE NAME                               # Vmotaj
    	@Override
        public CodeFragment visitVmotaj(pankoParser.VmotajContext ctx) {
    		CodeFragment addressCode = visit(ctx.address()); 
    		String value_register = this.generateNewRegister(); 
    		
            ST template = new ST(
            		"<address_code>" + 
                    "<value_register> = call i32 @scanInt()\n" +
                    "store i32 <value_register>, i32* <mem_register>\n"
            );
            template.add("address_code", addressCode.toString());
            template.add("mem_register", addressCode.getRegister());
            template.add("value_register", value_register);
            
            return new CodeFragment(template.render(), value_register);
        }
    	
    	@Override
        public CodeFragment visitVymotaj(pankoParser.VymotajContext ctx) {
                CodeFragment code = visit(ctx.rexpression());
                ST template = new ST(
                        "<value_code>" + 
                        "call i32 @printInt (i32 <value>)\n"
                );
                template.add("value_code", code);
                template.add("value", code.getRegister());
                
                CodeFragment ret = new CodeFragment();
                ret.addCode(template.render());
                return ret;
        }

    	public CodeFragment generateVariableDefine(String identifier, CodeFragment valueCode, String exists_error){
            CodeFragment memCode = null; 
            
            if (!mem.containsKey(identifier)) {
            		String mem_register = this.generateNewRegister(); 
                    mem.put(identifier, mem_register);
                    memCode = new CodeFragment(mem_register + " = alloca i32\n", mem_register);
                    
                    //this should be no problem: 
                	if(this.functions.containsKey(identifier)){
                		System.err.println("Warning: Variable '"+identifier+"' has same name as some function (generateDefineAssign)");
                	}
            } else {
                    memCode = new CodeFragment("", mem.get(identifier));
                    
                    System.err.println(String.format(exists_error, identifier));
                    if (this.inFunctionDefine){
                    	registerLocalVariable(identifier, "Warning: Global variable '" + identifier + "' was shadowed by parameter/variable of function '" + inFunctionName + "' (generateDefineAssign)");
                    }
            }
            
            return generateAssign(memCode, valueCode);
    	}
    	
        //TODO type 
    	//TODO block 
        @Override
        public CodeFragment visitVariableDefine(pankoParser.VariableDefineContext ctx) {
        	String identifier = ctx.NAME().getText();
        	
            if (this.inFunctionDefine){
        		registerLocalVariable(identifier, "Warning: Global variable '" + identifier + "' was shadowed by parameter of function '" + inFunctionName + "' (generateDefineAssign)");
            }
            
        	return generateVariableDefine(identifier, visit(ctx.rexpression()), "Warning: identifier '%s' already exists (PAN)");
        }

        //TODO check type 
        @Override
        public CodeFragment visitVariableAddress(pankoParser.VariableAddressContext ctx) {
        	return this.generateGetVariableAddress(ctx.NAME().getText()); 
        }
        
        private CodeFragment generateGetVariableAddress(String var_name){
        	if(this.mem.containsKey(var_name)){
        		return new CodeFragment("", this.mem.get(var_name)); 
	        }
	        else{
	        	System.err.println("Warning: Non-existing variable '"+var_name + "' - address to new i32(0) returned (visitVariableAddress)");
        		return new CodeFragment(
        				generateVariableDefine(var_name, generateConstant("0"), null).toString(), 
        				this.mem.get(var_name)
        		); 
	        }
        }
        
        //address: 
        //    NAME                                             # VariableAddress
        //    | ROLKA rvalue NAME                                # ArrayAddress  
        @Override
        public CodeFragment visitAddressValue(pankoParser.AddressValueContext ctx) {
        	return generateGetAddressValue(visit(ctx.address()));
        }
        
        //     | NAMOTAJ address rexpression                    # Assign 
        @Override
        public CodeFragment visitAssign(pankoParser.AssignContext ctx) {
        	return this.generateAssign(visit(ctx.address()), visit(ctx.rexpression())); 
        }
        
        private CodeFragment generateAssign(CodeFragment addressCode, CodeFragment valueCode) {
        	ST template = new ST(
        			"<address_code>" + 
                	"<value_code>" + 
                    "store i32 <value_reg>, i32* <address_reg>\n"
        	);
        	template.add("address_code", addressCode.toString());
        	template.add("value_code", valueCode.toString());
        	template.add("address_reg", addressCode.getRegister());
        	template.add("value_reg", valueCode.getRegister());
            
	        return new CodeFragment(template.render(), valueCode.getRegister());
        }
        
        private CodeFragment generateGetAddressValue(CodeFragment addressCode){
            String value_reg = generateNewRegister();
            
        	ST template = new ST(
        			"<address_code>" + 
        			"<value_reg> = load i32* <address_reg>\n"
        	);
        	template.add("address_code", addressCode.toString());
        	template.add("value_reg", value_reg);
        	template.add("address_reg", addressCode.getRegister());
            
	        return new CodeFragment(template.render(), value_reg);
        }
        
        private CodeFragment generateGetIdentifierValue(String identifier){
        	if(!mem.containsKey(identifier)){
        		System.err.println("Warning: Identifier '"+identifier+"' not know (generateGetIdentifierValue)");
        		return generateConstant("0");
        	}
        	return generateGetAddressValue(new CodeFragment("", mem.get(identifier))); 
        }
        
        //====================================== ARRAYS =================================

        //TODO NAME checks => remember type 
        //TODO free array at the end 
        //| WCBOOK rexpression TYPE NAME                   # ArrayDefine
        @Override
        public CodeFragment visitArrayDefine(pankoParser.ArrayDefineContext ctx) {
        	String arr_name = ctx.NAME().getText(); 
        	String arr_type = ctx.TYPE().getText(); 
        	String arr_reg = generateNewRegister();
        	this.mem.put(arr_name, arr_reg); 
        	
        	String mem_reg = generateNewRegister();
        	String size_reg = generateNewRegister(); 
        	
        	CodeFragment evalCountCode = visit(ctx.rexpression()); 
        	
        	ST template = new ST(
        			"<eval>" + 
        		    "<arr_reg> = alloca i32*\n" +  
        			"<size_reg> = mul i32 <variable_size>, <count_reg>\n" +
        		    "<mem_reg> = call noalias i32* @MALLOC(i32 <size_reg>) nounwind\n" +  
        		    "store i32* <mem_reg>, i32** <arr_reg>\n" 
        	);
        	template.add("variable_size", "4"); 
        	template.add("eval", evalCountCode.toString());
        	template.add("arr_reg", arr_reg);
        	template.add("mem_reg", mem_reg);
        	template.add("count_reg", evalCountCode.getRegister());
        	template.add("size_reg", size_reg);
        	
        	return new CodeFragment(template.render(), null);
        }

        @Override 
        public CodeFragment visitArrayAddress(pankoParser.ArrayAddressContext ctx) {
        	return generateGetIndexAddress(ctx.NAME().getText(), visit(ctx.rvalue())); 
        }
        
        public CodeFragment generateGetIndexAddress(String arr_name, CodeFragment indexEvalCode){
        	String arr_reg = mem.get(arr_name); 
        	
        	String mem_reg = generateNewRegister(); 
        	
        	String index_ptr_reg = generateNewRegister(); 
        	
        	ST template = new ST(
        			"<index_eval>" + 
        		    "<mem_reg> = load i32** <arr_reg>\n" +
        		    "<index_ptr_reg> = getelementptr inbounds i32* <mem_reg>, i32 <index_reg>\n"
        	);
        	template.add("arr_reg", arr_reg);
        	template.add("mem_reg", mem_reg);
        	template.add("index_eval", indexEvalCode.toString());
        	template.add("index_reg", indexEvalCode.getRegister());
        	template.add("index_ptr_reg", index_ptr_reg);

        	return new CodeFragment(template.render(), index_ptr_reg);
        }
        //======================================= FUNCTIONS =========================================
        
        /** 
    MOTAC TYPE NAME (TYPE NAME)* NEWLINE 
    (statements NEWLINE)?
    VYPAPAJ rexpression 
    ;  
         */
        
        private void registerLocalVariable(String var_name, String err){
    		if(mem.containsKey(var_name)){
    			System.err.println(err);
    			paramsShadow.put(var_name, mem.get(var_name)); 
    		} 
    		registeredLocalVariables.add(var_name); 
        }
        
        private void unregisterLocalVariables(){
            for(String var_name : this.registeredLocalVariables){
            	mem.remove(var_name);
            	if(paramsShadow.containsKey(var_name)){
            		mem.put(var_name, paramsShadow.get(var_name)); 
            	}
            }
            this.registeredLocalVariables.clear(); 
        }

        @Override
        public CodeFragment visitMain(pankoParser.MainContext ctx) {
        	if(this.functions.containsKey(FUNCTION_MAIN_NAME)){
        		System.err.println("Warning: MEGA MOTAC '"+ctx.funkcia().NAME().get(0).getText()+"' already defined (visitMain).");
        	}
        	return generateFunction(ctx.funkcia(), FUNCTION_MAIN_NAME); 
        }
        
        @Override
        public CodeFragment visitFunctionDefine(pankoParser.FunctionDefineContext ctx) {
        	return generateFunction(ctx.funkcia(), null); 
        }
        
        private CodeFragment generateFunction(pankoParser.FunkciaContext ctx, String override_name){
        	List<TerminalNode> tokens = ctx.NAME();
        	String fn_name = (override_name == null) ? tokens.get(0).getText() : override_name;
        	
        	if(this.inFunctionDefine){
    			System.err.println("Warning: Defining function '" + fn_name + "' in function. It will be ignored. (FUNCTION_DEFINE)");
    			return new CodeFragment(); 
        	}
        	if(this.mem.containsKey(fn_name)){
        		System.err.println("Warning: Function '"+fn_name+"' has same name as some variable (FUNCTION_DEFINE)");
        	}
        	if(override_name != null && "_main".equals(override_name) && tokens.size() > 1){
        		System.err.println("Warning: Main '"+ fn_name + "'cannot have parameters.");
        	}
        	
        	this.inFunctionDefine = true; 
        	this.inFunctionName = fn_name;
        	
        	FunctionFragment functionFragment = new FunctionFragment();

    		functionFragment.llvm_name = generateNewFunction();
    		this.functions.put(fn_name, functionFragment); 
    		
        	String paramDefinitionsCode = ""; 
        	String paramVariablesCode = ""; 
        	
        	//TODO types 
        	for(int i=1; i<tokens.size() ; i++){
        		String var_name = tokens.get(i).getText(); 
        		functionFragment.params.add(var_name);
        		
        		String register = generateNewRegister();
        		if(i != 1) paramDefinitionsCode += ","; 
        		paramDefinitionsCode += "i32 " + register;
        		
        		registerLocalVariable(var_name, "Warning: Global variable '" + var_name + "' was shadowed by parameter of function '"+fn_name+"' (FUNCTION_DEFINE)");
        		mem.remove(var_name);
        		
        		// generateDefineAssign adds it to this.mem
        		CodeFragment defineVariable = generateVariableDefine(var_name, new CodeFragment("", register), "PLACE petrz : exists error '"+var_name+"' (generateFunction)");
        		paramVariablesCode += defineVariable.toString(); 
        	}
        	
        	CodeFragment statementsCode = (ctx.statements() == null) ? null : visit(ctx.statements());
        	CodeFragment returnCode = visit(ctx.rexpression()); 

            ST template_body = new ST(
            		"<param_variables>" + 
                    "<statements>" +
                    "<return_code>" + 
                    "ret i32 <return_register>\n"
            );
            template_body.add("param_variables", paramVariablesCode);
            template_body.add("statements", statementsCode);
            template_body.add("return_code", returnCode);
            template_body.add("return_register", returnCode.getRegister());
            
            functionFragment.body_code = new CodeFragment(template_body.render(), returnCode.getRegister());
            
            ST template = new ST(
            		";<function_name>\n" + 
                    "define i32 <llvm_name>(<params_code>){\n" +
                    "    entry:\n" + 
                    "<body_code>" + 
                    "}" + "\n"
            );
            template.add("function_name", fn_name);
            template.add("llvm_name", functionFragment.llvm_name);
            template.add("params_code", paramDefinitionsCode);
            template.add("body_code", functionFragment.body_code.toString());
            
            functionFragment.all_code = new CodeFragment(template.render(), returnCode.getRegister()); 
            
            //clean up parameter variables 
            unregisterLocalVariables(); 
            
            this.inFunctionDefine = false; 
            
            //function code must be at the beginning 
            return new CodeFragment(); 
        }
        
        //TODO type
        /**
         *  ZMOTAJ NAME (rvalue*) (rexpression)?             # FunctionValue
         */
        @Override
        public CodeFragment visitFunctionValue(pankoParser.FunctionValueContext ctx) {
        	String fn_name = ctx.NAME().getText();
        	FunctionFragment fn_fragment = this.functions.get(fn_name); 
        	
        	if(fn_fragment == null){
        		System.err.println("Warning: Not recognized function '"+fn_name+"' (FUNCTION_VALUE).");
        		return generateConstant("0");
        	}
        	
        	//evaluate parameter expressions and generate param string 
        	List<CodeFragment> paramEvals = new ArrayList<CodeFragment>();
        	
        	if(ctx.rvalue() != null){
        		for(int i=0; i<ctx.rvalue().size(); i++){
        			paramEvals.add(visit(ctx.rvalue(i)));  
        		}
        	}
        	if(ctx.rexpression() != null){
        		paramEvals.add(visit(ctx.rexpression())); 
        	}

        	String evalCode = ""; 
        	String paramsCode = ""; 
        	
        	for(CodeFragment cf : paramEvals){
        		if(paramsCode.length() > 0){
        			paramsCode += ",";
        		}
        		paramsCode += "i32 " + cf.getRegister();
        		
        		evalCode += cf.toString(); 
        	}

        	String fn_register = generateNewRegister();
        	
        	//put all together
            ST template = new ST( 
            		"<eval_params>" + 
                    "<fn_reg> = call i32 <llvm_name>(<parameters>)\n"
            );
            template.add("fn_reg", fn_register);
            template.add("llvm_name", fn_fragment.llvm_name);
            template.add("eval_params", evalCode); 
            template.add("parameters", paramsCode); 
                        
            CodeFragment ret = new CodeFragment();
            ret.setRegister(fn_register);
            ret.addCode(template.render());

            return ret;
        }

        @Override 
        public CodeFragment visitIf(pankoParser.IfContext ctx) {
            CodeFragment condition = visit(ctx.rexpression());
            CodeFragment statement_true = visit(ctx.tr);
            CodeFragment statement_false = (ctx.fa != null) ? visit(ctx.fa) : new CodeFragment();

            ST template = new ST(
                    "; -- AGE? begin\n" +
                    "<condition_code>" + 
                    "<cmp_reg> = icmp ne i32 <con_reg>, 0\n" + 
                    "br i1 <cmp_reg>, label %<block_true>, label %<block_false>\n" +
                    "<block_true>:\n" +
                    "<statement_true_code>" +
                    "br label %<block_end>\n" + 
                    "<block_false>:\n" + 
                    "<statement_false_code>" +
                    "br label %<block_end>\n" + 
                    "<block_end>:\n" +
                    "<ret> = add i32 0, 0\n" + 
                    "; -- AGE? end\n"
            );
            template.add("condition_code", condition);
            template.add("statement_true_code", statement_true);
            template.add("statement_false_code", statement_false);
            template.add("cmp_reg", this.generateNewRegister());
            template.add("con_reg", condition.getRegister());
            template.add("block_true", this.generateNewLabel());
            template.add("block_false", this.generateNewLabel());
            template.add("block_end", this.generateNewLabel());
            String return_register = generateNewRegister();
            template.add("ret", return_register);
            
            CodeFragment ret = new CodeFragment();
            ret.setRegister(return_register);
            ret.addCode(template.render());

            return ret;
        }

        @Override
        public CodeFragment visitWhile(pankoParser.WhileContext ctx) {
                CodeFragment condition = visit(ctx.rexpression());
                CodeFragment body = visit(ctx.statement());
                
                ST template = new ST(
                        "; -- MACKAJ begin\n" +
                		"br label %<cmp_label>\n" + 
                        "<cmp_label>:\n" + 
                        "<condition_code>" +
                        "<cmp_register> = icmp ne i32 <condition_register>, 0\n" + 
                        "br i1 <cmp_register>, label %<body_label>, label %<end_label>\n" + 
                        "<body_label>:\n" + 
                        "<body_code>" + 
                        "br label %<cmp_label>\n" + 
                        "<end_label>:\n" + 
                        "<ret> = add i32 0, 0\n" +
                        "; -- MACKAJ end\n"
                );
                template.add("cmp_label", generateNewLabel());
                template.add("condition_code", condition);
                template.add("cmp_register", generateNewRegister());
                template.add("condition_register", condition.getRegister());
                template.add("body_label", generateNewLabel());
                template.add("end_label", generateNewLabel());
                template.add("body_code", body);
                String end_register = generateNewRegister();
                template.add("ret", end_register);
                
                CodeFragment ret = new CodeFragment();
                ret.addCode(template.render());
                ret.setRegister(end_register);
                return ret;
        }
        
        //TODO refactor it to WHILE 
        //TODO notworking if already defined (used before) :
//POCHIPUJ i 42
//  NAMOTAJ ROLKA i arr * i i
//POCHIPUJ i 42
//  VYMOTAJ ROLKA i arr
        @Override
        public CodeFragment visitFor(pankoParser.ForContext ctx) {
        		String identifier = ctx.NAME().getText();
        		
                CodeFragment statement_najprv = generateVariableDefine(identifier, generateConstant("0"), "Warning: (POCHIPUJ-najprv) identifier '%s' already exists");
                CodeFragment statement_motaj = visit(ctx.statement());
                //identifier = identifier + 1
                CodeFragment statement_potom = generateAssign(
                	this.generateGetVariableAddress(identifier), 
                	generateBinaryOperator(
                        generateGetIdentifierValue(identifier),
                        generateConstant("1"),
                        pankoParser.ADD
                    )
                );
                
                CodeFragment condition_value = generateBinaryOperator(
                		generateGetIdentifierValue(identifier),
                        visit(ctx.rexpression()),
                        pankoParser.SUB
                );
                
                ST template = new ST(
                        "; -- POCHIPUJ begin\n" +
                        "<statement_najprv_code>" + 
                        "br label %<body_label>\n" + 
                        "<cmp_label>:\n" + 
                        "<condition_code>" +
                        "<cmp_register> = icmp ne i32 <condition_register>, 0\n" + 
                        "br i1 <cmp_register>, label %<body_label>, label %<end_label>\n" + 
                        "<body_label>:\n" + 
                        "<statement_motaj_code>" + 
                        "<statement_potom_code>" + 
                        "br label %<cmp_label>\n" + 
                        "<end_label>:\n" + 
                        "<ret> = add i32 0, 0\n" +
                        "; -- POCHIPUJ end\n"
                );
                
                template.add("cmp_label", generateNewLabel());
                template.add("condition_code", condition_value);
                template.add("cmp_register", generateNewRegister());
                template.add("condition_register", condition_value.getRegister());
                template.add("body_label", generateNewLabel());
                template.add("end_label", generateNewLabel());
                template.add("statement_najprv_code", statement_najprv);
                template.add("statement_potom_code", statement_potom);
                template.add("statement_motaj_code", statement_motaj);
                String end_register = generateNewRegister();
                template.add("ret", end_register);
                
                CodeFragment ret = new CodeFragment();
                ret.addCode(template.render());
                ret.setRegister(end_register);
                return ret;
        }
        
        //TODO type 
    	public CodeFragment generateConstant(String value){
            CodeFragment code = new CodeFragment();
            String register = generateNewRegister();
            code.setRegister(register);
            code.addCode(String.format("%s = add i32 0, %s\n", register, value));
            return code;
    	}
    	
    	//TODO type 
        public CodeFragment generateBinaryOperator(CodeFragment left, CodeFragment right, Integer operator) {
                String code_stub = "<ret> = <instruction> i32 <left_val>, <right_val>\n";
                String instruction = "or";
                switch (operator) {
                        case pankoParser.ADD:
                                instruction = "add";
                                break;
                        case pankoParser.SUB:
                                instruction = "sub";
                                break;
                        case pankoParser.MUL:
                                instruction = "mul";
                                break;
                        case pankoParser.DIV:
                                instruction = "sdiv";
                                break;
                        case pankoParser.MOD:
                            instruction = "srem";
                            break;
                        case pankoParser.EXP:
                                instruction = "@iexp";
                                code_stub = "<ret> = call i32 <instruction>(i32 <left_val>, i32 <right_val>)\n";
                                break;
                        case pankoParser.AND:
                                instruction = "and";
                        case pankoParser.OR:
                                ST temp1 = new ST(
                                        "<r1> = icmp ne i32 \\<left_val>, 0\n" +
                                        "<r2> = icmp ne i32 \\<right_val>, 0\n" +
                                        "<r3> = \\<instruction> i1 <r1>, <r2>\n" +
                                        "\\<ret> = zext i1 <r3> to i32\n"
                                );
                                temp1.add("r1", this.generateNewRegister());
                                temp1.add("r2", this.generateNewRegister());
                                temp1.add("r3", this.generateNewRegister());
                                code_stub = temp1.render();
                                break;
                        case pankoParser.EQUAL:
                            ST temp2 = new ST(
                                    "<r1> = icmp eq i32 \\<left_val>, \\<right_val>\n" +
                                    "\\<ret> = zext i1 <r1> to i32\n"
                            );
                            temp2.add("r1", this.generateNewRegister());
                            code_stub = temp2.render(); 
                        	break;
                        case pankoParser.SMALLER:
                            ST temp3 = new ST(
                                    "<r1> = icmp slt i32 \\<left_val>, \\<right_val>\n" +
                                    "\\<ret> = zext i1 <r1> to i32\n"
                            );
                            temp3.add("r1", this.generateNewRegister());
                            code_stub = temp3.render();
                            break;
                }
                ST template = new ST(
                        "<left_code>" + 
                        "<right_code>" + 
                        code_stub
                );
                template.add("left_code", left);
                template.add("right_code", right);
                template.add("instruction", instruction);
                template.add("left_val", left.getRegister());
                template.add("right_val", right.getRegister());
                String ret_register = this.generateNewRegister();
                template.add("ret", ret_register);
                
                CodeFragment ret = new CodeFragment();
                ret.setRegister(ret_register);
                ret.addCode(template.render());
                return ret;
        
        }
        
        //TODO type 
        public CodeFragment generateUnaryOperator(CodeFragment code, Integer operator) {
                if (operator == pankoParser.ADD) {
                        return code;
                }

                String code_stub = "";
                switch(operator) {
                        case pankoParser.SUB:
                                code_stub = "<ret> = sub i32 0, <input>\n";
                                break;
                        case pankoParser.NOT:
                                ST temp = new ST(
                                        "<r> = icmp eq i32 \\<input>, 0\n" + 
                                        "\\<ret> = zext i1 <r> to i32\n"
                                );
                                temp.add("r", this.generateNewRegister());
                                code_stub = temp.render();
                                break;
                }
                ST template = new ST("<code>" + code_stub);
                String ret_register = this.generateNewRegister();
                template.add("code", code);
                template.add("ret", ret_register);
                template.add("input", code.getRegister());

                CodeFragment ret = new CodeFragment();        
                ret.setRegister(ret_register);
                ret.addCode(template.render());
                return ret;
                
        }
    	

        @Override 
        public CodeFragment visitBlock(pankoParser.BlockContext ctx) {
                return visit(ctx.statements());
        }
}
