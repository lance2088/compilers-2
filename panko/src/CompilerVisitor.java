import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.misc.NotNull;
import org.stringtemplate.v4.*;

public class CompilerVisitor extends pankoBaseVisitor<CodeFragment> {
        private Map<String, String> mem = new HashMap<String, String>();
        private int labelIndex = 0;
        private int registerIndex = 0;

        private String generateNewLabel() {
                return String.format("L%d", this.labelIndex++);
        }

        private String generateNewRegister() {
                return String.format("%%R%d", this.registerIndex++);
        }

        @Override
        public CodeFragment visitInit(pankoParser.InitContext ctx) {
                CodeFragment body = visit(ctx.statements());

                ST template = new ST(
                        "declare i32 @printInt(i32)\n" + 
                        "declare i32 @iexp(i32, i32)\n" + 
                        "define i32 @main() {\n" + 
                        "start:\n" + 
                        "<body_code>" + 
                        "ret i32 0\n" +
                        "}\n"
                );
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
                CodeFragment code = visit(ctx.rvalue());
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

    	public CodeFragment generateDeclareAssign(String identifier, CodeFragment value, String identifier_error, String exists_error){
            String mem_register;
            String code_stub = "";

            if (!mem.containsKey(identifier)) {
                    mem_register = this.generateNewRegister();
                    code_stub = "<mem_register> = alloca i32\n";
                    mem.put(identifier, mem_register);
                    if(identifier_error != null){
                    	System.err.println(String.format(identifier_error, identifier));
                    }
            } else {
                    mem_register = mem.get(identifier);
                    if(exists_error != null){
                    	System.err.println(String.format(exists_error, identifier));
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
    	//PAN TYPE NAME rvalue                           # Declare
        @Override
        public CodeFragment visitDeclare(pankoParser.DeclareContext ctx) {
        	return generateDeclareAssign(ctx.NAME().getText(), visit(ctx.rvalue()), null, "Warning: (PAN) identifier '%s' already exists");
        }

    	//TODO block 
        //NAMOTAJ NAME rvalue                            # Assign
        @Override
        public CodeFragment visitAssign(pankoParser.AssignContext ctx) {
        	return generateDeclareAssign(ctx.NAME().getText(), visit(ctx.rvalue()), "Warning: (NAMOTAJ) identifier '%s' doesn't exists", null);
        }
        
        public CodeFragment generateGetValue(String id){
            CodeFragment code = new CodeFragment();
            String register = generateNewRegister();
            if (!mem.containsKey(id)) {
                    System.err.println(String.format("Warning: (NAME) identifier '%s' doesn't exists", id));
                    code.addCode(String.format("%s = add i32 0, 0\n", register));

            } else {
                    String pointer = mem.get(id);
                    code.addCode(String.format("%s = load i32* %s\n", register, pointer));
            }
            code.setRegister(register);
            return code;
        	
        }
        
        //TODO functions 
        @Override
        public CodeFragment visitVar(pankoParser.VarContext ctx) {
        	return generateGetValue(ctx.NAME().getText()); 
        }

        @Override 
        public CodeFragment visitIf(pankoParser.IfContext ctx) {
            CodeFragment condition = visit(ctx.rvalue());
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
                CodeFragment condition = visit(ctx.rvalue());
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
        
        @Override
        public CodeFragment visitFor(pankoParser.ForContext ctx) {
        		String identifier = ctx.NAME().getText(); 
                CodeFragment statement_najprv = generateDeclareAssign(identifier, generateConstant("0"), null, "Warning: (POCHIPUJ) identifier '%s' already exists");
                CodeFragment statement_motaj = visit(ctx.statement());
                //identifier = identifier + 1
                CodeFragment statement_potom = generateDeclareAssign(identifier, generateBinaryOperator(
                                generateGetValue(identifier),
                                generateConstant("1"),
                                pankoParser.ADD
                   ),
                   "Warning: (POCHIPUJ) identifier '%s' doesn't exists",
                   "Warning: (POCHIPUJ) identifier '%s' already exists"
                );
                
                CodeFragment condition_value = generateBinaryOperator(
                        generateGetValue(identifier),
                        visit(ctx.rvalue()),
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
