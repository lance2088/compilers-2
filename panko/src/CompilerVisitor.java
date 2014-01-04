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
        
        //these variables are treated differently (they don't need to be loaded) 
        private Set<String> functionParams = new HashSet<String>(); 

    	//global variables which were shadowed by function variables 
    	Map<String, String> paramsShadow = new HashMap<String, String>(); 
    	List<String> registeredLocalVariables = new ArrayList<String>();
        

        private String generateNewLabel() {
                return String.format("L%d", this.labelIndex++);
        }

        private String generateNewRegister() {
                return String.format("%%R%d", this.registerIndex++);
        }
        
        private String generateNewFunction() {
            return String.format("@f%d", this.functionIndex++);
        }

        private String getFunctionDefinitions() {
        	String result = "";
        	for(Map.Entry<String, FunctionFragment> entry : functions.entrySet()){
        		result += entry.getValue().code.toString(); 
        	}
        	return result; 
        }
        
        @Override
        public CodeFragment visitInit(pankoParser.InitContext ctx) {
                CodeFragment body = visit(ctx.statements());

                ST template = new ST(
                        "declare i32 @printInt(i32)\n" + 
                        "declare i32 @iexp(i32, i32)\n" +
                        "declare i32* @MALLOC(i32)\n" +  
                        "declare i32 @FREE(i32*)\n" +  
                        "<function_definitions>\n" + 
                        "define i32 @main() {\n" + 
                        "start:\n" + 
                        "<body_code>" + 
                        "ret i32 0\n" +
                        "}\n"
                );
                template.add("function_definitions", this.getFunctionDefinitions());
                template.add("body_code", body);

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
        
        @Override
    	public CodeFragment visitSuchy(@NotNull pankoParser.SuchyContext ctx){
            return new CodeFragment();
    	}
        
        @Override
    	public CodeFragment visitEmp(@NotNull pankoParser.EmpContext ctx){
            return new CodeFragment();
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

    	public CodeFragment generateDefineAssign(String identifier, CodeFragment value, String identifier_error, String exists_error){
            String mem_register;
            String code_stub = "";
            
            if (!mem.containsKey(identifier)) {
                    mem_register = this.generateNewRegister();
                    code_stub = "<mem_register> = alloca i32\n";
                    mem.put(identifier, mem_register);
                    if(identifier_error != null){
                    	System.err.println(String.format(identifier_error, identifier));
                    }
                	if(this.functions.containsKey(identifier)){
                		System.err.println("Warning: Variable '"+identifier+"' has same name as some function (generateDefineAssign)");
                	}
            } else {
                    mem_register = mem.get(identifier);
                    if(exists_error != null){
                    	System.err.println(String.format(exists_error, identifier));
                        if (this.inFunctionDefine){
                    		registerLocalVariable(identifier, "Warning: Global variable '" + identifier + "' was shadowed by parameter/variable of function '" + inFunctionName + "' (generateDefineAssign)");
                        }
                    }
            }
            ST template = new ST(
                    "<value_code>" + 
                    code_stub + 
                    "store i32 <value_register>, i32* <mem_register>\n"
            );
            template.add("value_code", value);
            template.add("value_register", value.getRegister());
            template.add("mem_register", mem_register);
            CodeFragment ret = new CodeFragment();
            ret.addCode(template.render());
            ret.setRegister(value.getRegister());
            return ret;
    		
    	}
    	
        //TODO type 
    	//TODO block 
    	//PAN TYPE NAME rexpression                           # Declare
        @Override
        public CodeFragment visitVariableDefine(pankoParser.VariableDefineContext ctx) {
        	String identifier = ctx.NAME().getText();
        	
            if (this.inFunctionDefine){
        		registerLocalVariable(identifier, "Warning: Global variable '" + identifier + "' was shadowed by parameter of function '" + inFunctionName + "' (generateDefineAssign)");
            }
            
        	return generateDefineAssign(identifier, visit(ctx.rexpression()), null, "Warning: (PAN) identifier '%s' already exists");
        }

    	//TODO block 
        //NAMOTAJ NAME rexpression                            # Assign
        @Override
        public CodeFragment visitVariableAssign(pankoParser.VariableAssignContext ctx) {
        	return generateDefineAssign(ctx.NAME().getText(), visit(ctx.rexpression()), "Warning: (NAMOTAJ) identifier '%s' doesn't exists", null);
        }
        
        @Override
        public CodeFragment visitVariableValue(pankoParser.VariableValueContext ctx) {
        	return generateGetValue(ctx.NAME().getText()); 
        }
        
        public CodeFragment generateGetValue(String id){
            CodeFragment code = new CodeFragment();
            
            //it's a global variable
            if(!this.functionParams.contains(id)){
                String register = generateNewRegister();
	            if (!mem.containsKey(id)) {
	                    System.err.println(String.format("Warning: (NAME) identifier '%s' doesn't exists", id));
	                    return generateConstant("0");
	            } else {
	                    String pointer = mem.get(id);
	                    code.addCode(String.format("%s = load i32* %s\n", register, pointer));
	            }
	            code.setRegister(register);
            }
            //it's a function parameter
            else{
            	if(!mem.containsKey(id)){
            		System.err.println("PLACES Petrz (generateGetValue)");
            	}
            	else{
            		code.setRegister(mem.get(id));
            	}
            }
            
            return code;
        	
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
        			"<size_reg> = mul i32 4, <count_reg>\n" +
        		    "<mem_reg> = call noalias i32* @MALLOC(i32 <size_reg>) nounwind\n" +  
        		    "store i32* <mem_reg>, i32** <arr_reg>\n" 
        	);
        	template.add("eval", evalCountCode.toString());
        	template.add("arr_reg", arr_reg);
        	template.add("mem_reg", mem_reg);
        	template.add("count_reg", evalCountCode.getRegister());
        	template.add("size_reg", size_reg);
        	
        	return new CodeFragment(template.render(), null);
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
        
        //TODO NAME checks 
        //| NAMOTAJ ROLKA rvalue NAME rexpression          # ArrayAssign 
        @Override 
        public CodeFragment visitArrayAssign(pankoParser.ArrayAssignContext ctx) {
        	String arr_name = ctx.NAME().getText(); 
        	
        	CodeFragment getIndexAddressCode = generateGetIndexAddress(arr_name, visit(ctx.rvalue()));
        	
        	CodeFragment valueEvalCode = visit(ctx.rexpression()); 
        	
        	ST template = new ST(
        			"<get_index_address>" +
                    "<value_eval>" + 
        		    "store i32 <value_reg>, i32* <index_ptr_reg>\n" 
        	);
        	template.add("get_index_address", getIndexAddressCode.toString());
        	template.add("index_ptr_reg", getIndexAddressCode.getRegister());
        	template.add("value_eval", valueEvalCode.toString());
        	template.add("value_reg", valueEvalCode.getRegister());

        	return new CodeFragment(template.render(), valueEvalCode.getRegister());
        }

        //TODO NAME checks 
        //   | FREE NAME                                      # ArrayDelete
        @Override 
        public CodeFragment visitArrayDelete(pankoParser.ArrayDeleteContext ctx) {
        	String arr_name = ctx.NAME().getText(); 
        	String arr_reg = mem.get(arr_name); 
        	
        	String mem_reg = generateNewRegister(); 
        	
        	ST template = new ST(
        			"<mem_reg> = load i32** <arr_reg>\n" +
        			"call i32 @FREE(i32* <mem_reg>)\n"
        	);
        	template.add("arr_reg", arr_reg);
        	template.add("mem_reg", mem_reg);
        	
        	return new CodeFragment(template.render(), null);
        }
        
        //| ROLKA rvalue NAME                          # ArrayValue 
        public CodeFragment visitArrayValue(pankoParser.ArrayValueContext ctx) {
        	String arr_name = ctx.NAME().getText(); 
        	
        	CodeFragment getIndexAddressCode = generateGetIndexAddress(arr_name, visit(ctx.rvalue()));
        	
        	String value_reg = generateNewRegister(); 
        	
        	ST template = new ST(
        			"<get_index_address>" +
        		    "<value_reg> = load i32* <index_ptr_reg>\n" 
        	);
        	template.add("get_index_address", getIndexAddressCode.toString());
        	template.add("index_ptr_reg", getIndexAddressCode.getRegister());
        	template.add("value_reg", value_reg);

        	return new CodeFragment(template.render(), value_reg);
        }
        
        //======================================= FUNCTIONS =========================================
        //TODO functions 
        @Override
        public CodeFragment visitMain(pankoParser.MainContext ctx) {
        	return new CodeFragment(); 
        }
        
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
        public CodeFragment visitFunctionDefine(pankoParser.FunctionDefineContext ctx) {
        	List<TerminalNode> tokens = ctx.funkcia().NAME();
        	String fn_name = tokens.get(0).getText();
        	
        	if(this.inFunctionDefine){
    			System.err.println("Warning: Defining function '" + fn_name + "' in function. It will be ignored. (FUNCTION_DEFINE)");
    			return new CodeFragment(); 
        	}
        	
        	this.inFunctionDefine = true; 
        	this.inFunctionName = fn_name;
        	
        	FunctionFragment functionFragment = new FunctionFragment();

    		functionFragment.llvm_name = generateNewFunction();
    		this.functions.put(fn_name, functionFragment); 
    		
        	String paramsCode = ""; 
        	
        	//TODO types 
        	for(int i=1; i<tokens.size() ; i++){
        		String var_name = tokens.get(i).getText(); 
        		functionFragment.params.add(var_name);
        		//so VisitVariableValue knows that it is a function parameter 
        		this.functionParams.add(var_name); 
        		
        		String register = generateNewRegister();
        		if(i != 1) paramsCode += ","; 
        		paramsCode += "i32 " + register;
        		
        		registerLocalVariable(var_name, "Warning: Global variable '" + var_name + "' was shadowed by parameter of function '"+fn_name+"' (FUNCTION_DEFINE)");
        		mem.put(var_name, register);
        	}
        	
        	if(this.mem.containsKey(fn_name)){
        		System.err.println("Warning: Function '"+fn_name+"' has same name as some variable (FUNCTION_DEFINE)");
        	}
        	
        	CodeFragment body = (ctx.funkcia().statements() == null) ? null : visit(ctx.funkcia().statements());
        	CodeFragment result = visit(ctx.funkcia().rexpression()); 
        	
        	/*
        	define i32 @mul_add(i32 %x, i32 %y, i32 %z) {
        		entry:
        		  %tmp = mul i32 %x, %y
        		  %tmp2 = add i32 %tmp, %z
        		  ret i32 %tmp2
        		}
        		*/
            ST template = new ST(
                    "define i32 <llvm_name>(<code_variables>){\n" +
                    "    entry:\n" + 
                    "<body>" +
                    "<return_code>" + 
                    "ret i32 <return_register>\n" +
                    "}" + "\n"
            );
            template.add("llvm_name", functionFragment.llvm_name);
            template.add("code_variables", paramsCode);
            template.add("body", body);
            template.add("return_code", result);
            template.add("return_register", result.getRegister());
            
            CodeFragment ret = new CodeFragment(); 
            ret.addCode(template.render());
            ret.setRegister(result.getRegister());
            functionFragment.code = ret; 
            
            //clean up parameter variables 
            unregisterLocalVariables(); 
            this.functionParams.clear();
            
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
        		
                CodeFragment statement_najprv = generateDefineAssign(identifier, generateConstant("0"), null, "Warning: (POCHIPUJ-najprv) identifier '%s' already exists");
                CodeFragment statement_motaj = visit(ctx.statement());
                //identifier = identifier + 1
                CodeFragment statement_potom = generateDefineAssign(identifier, generateBinaryOperator(
                                generateGetValue(identifier),
                                generateConstant("1"),
                                pankoParser.ADD
                   ),
                   "Warning: (POCHIPUJ-potom) identifier '%s' doesn't exists - PLACES petrz",
                   null// "Warning: (POCHIPUJ-potom) identifier '%s' already exists" //ofc it exists as I defined it in statement_najprv :P 
                );
                
                CodeFragment condition_value = generateBinaryOperator(
                        generateGetValue(identifier),
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
