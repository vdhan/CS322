import java.util.*;

// **********************************************************************
// Ast class (base class for all other kinds of nodes)
// **********************************************************************
abstract class Ast {
}

class Program extends Ast {
    public Program(DeclList declList) {
        this.declList = declList;
    }

    public void check()
    {
    	declList.check();
    }
    
    public void compile() {
		System.out.println("Done!");
	}

    protected DeclList declList;
}

// **********************************************************************
// Decls
// **********************************************************************
class DeclList extends Ast {
    public DeclList(LinkedList<Decl> decls) {
        this.decls = decls;
    }
    
    public void check()
    {
    	LinkedList<String> names = new LinkedList<String>();
    	LinkedList<VarDecl> GlobalVars = new LinkedList<VarDecl>();

    	for(int i = 0; i < decls.size(); ++i)
    	{
    		for(int j = 0; j < names.size(); ++j)
    		{
    			if(decls.get(i).name.strVal.equals(names.get(j)))
    			{
    				if(decls.get(i).type.name.compareTo(decls.get(j).type.name) != 0)
    				{
    					Errors.semanticError(decls.get(i).name.getLine(), decls.get(i).name.getChar(),
    							"conflicting types for '" + decls.get(i).name.strVal + "'");
    				}

    				if(decls.get(i).getClass().toString().equals(decls.get(j).getClass().toString()) ||
    						decls.get(i).getClass().toString().equals("class VarDecl") ||
    						decls.get(j).getClass().toString().equals("class VarDecl"))
    				{
    					Errors.semanticError(decls.get(i).name.getLine(), decls.get(i).name.getChar(),
    							"'" + decls.get(i).name.strVal + "' redeclared as different kind of symbol");
    				}

    				if((decls.get(i).getClass().toString().equals("class FnPreDecl") &&
    						decls.get(j).getClass().toString().equals("class FnDecl")))
    				{
    					checkFormal(decls.get(i), decls.get(j));
    				}
    				else if((decls.get(j).getClass().toString().equals("class FnPreDecl") &&
    						decls.get(i).getClass().toString().equals("class FnDecl")))
    				{
    					checkFormal(decls.get(j), decls.get(i));
    				}
    			}
    		}
    		
    		if(decls.get(i).getClass().toString().equals("class VarDecl"))
    		{
    			VarDecl temp;
    			
    			if(decls.get(i).type.name.equals("void") && decls.get(i).type.numPointers < 1)
    			{
    				Errors.semanticError(decls.get(i).name.getLine(), decls.get(i).name.getChar(),
    						"storage size of '" + decls.get(i).name.strVal + "' isn’t known");
    			}
    			
    			temp = (VarDecl)decls.get(i);
    			GlobalVars.add(temp);
    		}
    		
    		names.add(decls.get(i).name.strVal);
    		
    		if(decls.get(i).getClass().toString().equals("class FnDecl"))
    		{
    			FnDecl temp = (FnDecl)decls.get(i);
    			temp.formalList.check();
    			temp.body.check(temp.formalList.formals, decls, GlobalVars, i, temp.type);
    		}
    		else if(decls.get(i).getClass().toString().equals("class FnPreDecl"))
    		{
    			FnPreDecl temp = (FnPreDecl)decls.get(i);
    			temp.formalList.check();
    		}
    	}
    }
    
    public static void checkFormal(Decl x, Decl y)
    {
		FnPreDecl a = (FnPreDecl)x;
		FnDecl b = (FnDecl) y;
		
		if(a.formalList.formals.size() != b.formalList.formals.size())
		{
			Errors.semanticWarn(b.name.getLine(), b.name.getChar(), "prototype declaration");
		}
		
		for(int i = 0; i < a.formalList.formals.size(); ++i)
		{
			if(a.formalList.formals.get(i).type.name.compareTo(b.formalList.formals.get(i).type.name) != 0)
			{
				Errors.semanticError(b.name.getLine(), b.name.getChar(),
						"conflicting types for '" + b.name.strVal + "'");
			}
			else
			{
				if(a.formalList.formals.get(i).type.numPointers != b.formalList.formals.get(i).type.numPointers)
				{
					Errors.semanticError(b.name.getLine(), b.name.getChar(),
							"conflicting types for '" + b.name.strVal + "'");
				}
			}
			
			if(a.formalList.formals.get(i).type.name.equals("void") && a.formalList.formals.get(i).type.numPointers < 1)
			{
				Errors.semanticWarn(a.name.getLine(), a.name.getChar(),
						"parameter " + (i + 1) + " ('" + a.name.strVal + "') has void type [enabled by default]");
			}
		}
    }
    
    // linked list of kids (Decls)
    protected LinkedList<Decl> decls;
}

abstract class Decl extends Ast {
	protected Type type;
    protected Id name;
}

class VarDecl extends Decl {
    public VarDecl(Type type, Id name) {
        this.type = type;
        this.name = name;
    }
}

class FnDecl extends Decl {
    public FnDecl(Type type, Id name, FormalsList formalList, FnBody body) {
        this.type = type;
        this.name = name;
        this.formalList = formalList;
        this.body = body;
    }
    
    protected FormalsList formalList;
    protected FnBody body;
}

class FnPreDecl extends Decl {
    public FnPreDecl(Type type, Id name, FormalsList formalList) {
        this.type = type;
        this.name = name;
        this.formalList = formalList;
    }
    
    protected FormalsList formalList;
}

class FormalsList extends Ast {
    public FormalsList(LinkedList<FormalDecl> formals) {
        this.formals = formals;
    }
    
    public void check() {
		for(int i = 0; i < formals.size(); ++i)
		{
			for(int j = 0; j < i; ++j)
			{
				if(formals.get(i).name.strVal.equals(formals.get(j).name.strVal))
				{
					Errors.semanticError(formals.get(i).name.getLine(), formals.get(i).name.getChar(),
							"redefinition of parameter '" + formals.get(i).name.strVal + "'");
				}
			}
			
			if(formals.get(i).type.name.equals("void") && formals.get(i).type.numPointers < 1)
			{
				Errors.semanticError(formals.get(i).name.getLine(), formals.get(i).name.getChar(),
						"parameter " + (i + 1) + " ('" + formals.get(i).name.strVal + "') has incomplete type");
			}
		}
	}
    
    // linked list of kids (FormalDecls)
    protected LinkedList<FormalDecl> formals;
}

class FormalDecl extends Decl {
    public FormalDecl(Type type, Id name) {
        this.type = type;
        this.name = name;
    }
}

class FnBody extends Ast {
	public FnBody(DeclList declList, StmtList stmtList)
	{
		this.declList = declList;
		this.stmtList = stmtList;
	}
	
	public void check(LinkedList<FormalDecl> formal, LinkedList<Decl> decl, LinkedList<VarDecl> GVars,
    		int order, Type type)
	{
    	LinkedList<VarDecl> vars = new LinkedList<VarDecl>();
    	
		for(int i = 0; i < declList.decls.size(); ++i)
		{
			if(declList.decls.get(i).type.name.equals("void") && declList.decls.get(i).type.numPointers < 1)
			{
				Errors.semanticError(declList.decls.get(i).name.getLine(), declList.decls.get(i).name.getChar(),
						"variable or field '" + declList.decls.get(i).name.strVal + "' declared void");
			}
			
			for(int j = 0; j < formal.size(); ++j)
			{
				if(declList.decls.get(i).name.strVal.equals(formal.get(j).name.strVal))
				{
					Errors.semanticError(declList.decls.get(i).name.getLine(), declList.decls.get(i).name.getChar(),
						"'" + declList.decls.get(i).name.strVal + "' redeclared as different kind of symbol");
				}
			}
			
			for(int j = 0; j < i; ++j)
			{
				if(declList.decls.get(i).name.strVal.equals(declList.decls.get(j).name.strVal))
				{
					Errors.semanticError(declList.decls.get(i).name.getLine(), declList.decls.get(i).name.getChar(),
						"conflicting types for '" + declList.decls.get(i).name.strVal + "'");
				}
			}
			
			vars.add((VarDecl)declList.decls.get(i));
		}
		 
		for(int i = 0; i < stmtList.stmts.size(); ++i)
		{
			if(stmtList.stmts.get(i).getClass().toString().equals("class CallStmt"))
			{
				stmtList.stmts.get(i).check(formal, vars, GVars, order, decl);
			}
			else
			{
				stmtList.stmts.get(i).check(formal, vars, GVars, order, decl, type);
			}
		}
	}
    
    protected DeclList declList;
    protected StmtList stmtList;
}

class StmtList extends Ast {
    public StmtList(LinkedList<Stmt> stmts) {
        this.stmts = stmts;
    }
    
    // linked list of kids (Stmts)
    protected LinkedList<Stmt> stmts;
}

// **********************************************************************
// Types
// **********************************************************************
class Type extends Ast {
    protected Type() {}
    
    public static Type CreateSimpleType(String name)
    {
        Type t = new Type();
        t.name = name;
        t.size = -1;
        t.numPointers = 0;
        
        return t;
    }
    
    public static Type CreateArrayType(String name, int size)
    {
        Type t = new Type();
        t.name = name;
        t.size = size;
        t.numPointers = 0;
        
        return t;
    }
    
    public static Type CreatePointerType(String name, int numPointers)
    {
        Type t = new Type();
        t.name = name;
        t.size = -1;
        t.numPointers = numPointers;
        
        return t;
    }
    
    public static Type CreateArrayPointerType(String name, int size, int numPointers)
    {
        Type t = new Type();
        t.name = name;
        t.size = size;
        t.numPointers = numPointers;
        
        return t;
    }
    
    public String name()
    {
        return name;
    }
    
    protected String name;
    protected int size;  // use if this is an array type
    protected int numPointers;
    
    public static final String voidTypeName = "void";
    public static final String intTypeName = "int";
}

// **********************************************************************
// Stmts
// **********************************************************************
abstract class Stmt extends Ast {
	public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decls, Type type) {}
	
	public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl) {}
	
	public void checkUnDecl(Exp exp, LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			LinkedList<Decl> decl, int order, Type type)
	{
		boolean t = false;
		
		if(exp.getClass().toString().equals("class Id"))
		{
			Id temp = (Id)exp;
			
			for(int i = 0; i < GVars.size(); ++i)
			{			
				if(temp.strVal.equals(GVars.get(i).name.strVal))
				{
					temp.type = GVars.get(i).type;
					t = true;
					break;
				}
			}
			
			if(!t)
			{
				for(int i = 0; i < formal.size(); ++i)
				{
					if(temp.strVal.equals(formal.get(i).name.strVal))
					{
						temp.type = formal.get(i).type;
						t = true;
						break;
					}
				}
			}
			
			if(!t)
			{
				for(int i = 0; i < vars.size(); ++i)
				{
					if(temp.strVal.equals(vars.get(i).name.strVal))
					{
						temp.type = vars.get(i).type;
						t = true;
						break;
					}
				}
			}
			
			if(!t)
			{
				Errors.semanticError(temp.getLine(), temp.getChar(), "'" + temp.strVal + "' undeclared");
			}
		}
		else if(exp.getClass().toString().equals("class CallExp"))
		{
	    	CallExp temp = (CallExp) exp;
	    	temp.check(formal, vars, GVars, order, decl);
		}
		else
		{
			exp.check(formal, vars, GVars, order, decl, type);
		}
	}
	
	public void checkDecl(DeclList decl)
	{
		for(int i = 0; i < decl.decls.size(); ++i)
		{
			for(int j = 0; j < i; ++j)
			{
				if(decl.decls.get(i).name.strVal.equals(decl.decls.get(j).name.strVal))
				{
					Errors.semanticError(decl.decls.get(i).name.getLine(), decl.decls.get(i).name.getChar(),
							"conflicting types for '" + decl.decls.get(i).name.strVal + "'");
				}
			}
			
			if(decl.decls.get(i).type.name.equals("void") && decl.decls.get(i).type.numPointers < 1)
			{
				Errors.semanticError(decl.decls.get(i).name.getLine(), decl.decls.get(i).name.getChar(),
						"variable or field '" + decl.decls.get(i).name.strVal + "' declared void");
			}
		}
	}
	
	public void updateVars(LinkedList<VarDecl> vars, LinkedList<Decl> decl)
	{
		for(int i = 0; i < decl.size(); ++i)
		{
			for(int j = 0; j < vars.size(); ++j)
			{
				if(vars.get(j).name.strVal.equals(decl.get(i).name.strVal))
				{
					vars.set(j, (VarDecl) decl.get(i));
					break;
				}
				
				vars.add((VarDecl) decl.get(i));
			}
		}
	}
	
	public void checkType(Exp a, Exp b)
	{
		if((a.type != null && b.type != null) &&
				(a.type.name.compareTo(b.type.name) != 0 || a.type.numPointers != b.type.numPointers))
		{
			Errors.semanticWarn(b.getLine(), b.getChar(), "incompatible types");
		}
	}
	
	public void checkType(Exp a, Type b)
	{
		if(a.type != null)
		{
			if(a.type.name.compareTo(b.name) != 0 || a.type.numPointers != b.numPointers)
			{
				Errors.semanticWarn(a.getLine(), a.getChar(), "incompatible types");
			}
		}
	}
}

class AssignStmt extends Stmt {
    public AssignStmt(Exp lhs, Exp exp) {
        this.lhs = lhs;
        this.exp = exp;
    }
    
    @Override
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> Gvars,
    		int order, LinkedList<Decl> decl, Type type) {
    	checkUnDecl(lhs, formal, vars, Gvars, decl, order, type);
    	checkUnDecl(exp, formal, vars, Gvars, decl, order, type);
    	
		checkType(lhs, exp);
	}
    
    protected Exp lhs;
    protected Exp exp;
}

class IfStmt extends Stmt {
    public IfStmt(Exp exp, DeclList declList, StmtList stmtList) {
        this.exp = exp;
        this.declList = declList;
        this.stmtList = stmtList;
    }
    
    @Override
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
    		int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp, formal, vars, GVars, decl, order, type);
    	checkDecl(declList);
    	updateVars(vars, declList.decls);
    	
    	for(int i = 0; i < stmtList.stmts.size(); ++i)
		{
			if(stmtList.stmts.get(i).getClass().toString().equals("class CallStmt"))
			{
				stmtList.stmts.get(i).check(formal, vars, GVars, order, decl);
			}
			else
			{
				stmtList.stmts.get(i).check(formal, vars, GVars, order, decl, type);
			}
		}
    }
    
    protected Exp exp;
    protected DeclList declList;
    protected StmtList stmtList;
}

class IfElseStmt extends Stmt {
    public IfElseStmt(Exp exp, DeclList declList1, StmtList stmtList1,
            DeclList declList2, StmtList stmtList2) {
        this.exp = exp;
        this.declList1 = declList1;
        this.stmtList1 = stmtList1;
        this.declList2 = declList2;
        this.stmtList2 = stmtList2;
    }
    
    @Override
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
    		int order, LinkedList<Decl> decl, Type type)
    {
    	LinkedList<VarDecl> vars2 = vars;
    	
    	checkUnDecl(exp, formal, vars, GVars, decl, order, type);
    	checkDecl(declList1);
    	updateVars(vars, declList1.decls);
    	
    	for(int i = 0; i < stmtList1.stmts.size(); ++i)
		{
			if(stmtList1.stmts.get(i).getClass().toString().equals("class CallStmt"))
			{
				stmtList1.stmts.get(i).check(formal, vars, GVars, order, decl);
			}
			else
			{
				stmtList1.stmts.get(i).check(formal, vars, GVars, order, decl, type);
			}
		}
    	
    	checkDecl(declList2);
    	updateVars(vars2, declList2.decls);
    	
    	for(int i = 0; i < stmtList2.stmts.size(); ++i)
		{
			if(stmtList2.stmts.get(i).getClass().toString().equals("class CallStmt"))
			{
				stmtList2.stmts.get(i).check(formal, vars2, GVars, order, decl);
			}
			else
			{
				stmtList2.stmts.get(i).check(formal, vars2, GVars, order, decl, type);
			}
		}
    }
    
    protected Exp exp;
    protected DeclList declList1;
    protected DeclList declList2;
    protected StmtList stmtList1;
    protected StmtList stmtList2;
}

class WhileStmt extends Stmt {
    public WhileStmt(Exp exp, DeclList declList, StmtList stmtList) {
        this.exp = exp;
        this.declList1 = declList;
        this.stmtList = stmtList;
    }
    
    @Override
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
    		int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp, formal, vars, GVars, decl, order, type);
    	checkDecl(declList1);
    	updateVars(vars, declList1.decls);
    	
    	for(int i = 0; i < stmtList.stmts.size(); ++i)
		{
			if(stmtList.stmts.get(i).getClass().toString().equals("class CallStmt"))
			{
				stmtList.stmts.get(i).check(formal, vars, GVars, order, decl);
			}
			else
			{
				stmtList.stmts.get(i).check(formal, vars, GVars, order, decl, type);
			}
		}
    }
    
    protected Exp exp;
    protected DeclList declList1;
    protected StmtList stmtList;
}

class ForStmt extends Stmt {
    public ForStmt(Stmt init, Exp cond, Stmt incr,
            DeclList declList1, StmtList stmtList) {
        this.init = init;
        this.cond = cond;
        this.incr = incr;
        this.declList1 = declList1;
        this.stmtList = stmtList;
    }
    
    @Override
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
    		int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(cond, formal, vars, GVars, decl, order, type);
    	init.check(formal, vars, GVars, order, decl, type);
    	incr.check(formal, vars, GVars, order, decl, type);
    	checkDecl(declList1);
    	updateVars(vars, declList1.decls);
    	
    	for(int i = 0; i < stmtList.stmts.size(); ++i)
		{
			if(stmtList.stmts.get(i).getClass().toString().equals("class CallStmt"))
			{
				stmtList.stmts.get(i).check(formal, vars, GVars, order, decl);
			}
			else
			{
				stmtList.stmts.get(i).check(formal, vars, GVars, order, decl, type);
			}
		}
    }
    
    protected Stmt init;
    protected Exp cond;
    protected Stmt incr;
    protected DeclList declList1;
    protected StmtList stmtList;
}

class CallStmt extends Stmt {
    public CallStmt(CallExp callExp) {
        this.callExp = callExp;
    }
    
    @Override
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl) {
    	callExp.check(formal, vars, GVars, order, decl);
	}
    
    protected CallExp callExp;
}

class ReturnStmt extends Stmt {
    public ReturnStmt(Exp exp) {
        this.exp = exp;
    }
    
    @Override
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
    		int order, LinkedList<Decl> decl, Type type)
    {
    	if(exp != null)
    	{
    		checkUnDecl(exp, formal, vars, GVars, decl, order, type);
    		checkType(exp, type);
    	}
    }
    
    protected Exp exp; // null for empty return
}

// **********************************************************************
// Exps
// **********************************************************************
abstract class Exp extends Ast {
    public abstract int getLine();
    public abstract int getChar();
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type) {}
    
	public void checkUnDecl(Exp exp, LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			LinkedList<Decl> decl, int order, Type type)
	{
		boolean t = false;
		
		if(exp.getClass().toString().equals("class Id"))
		{
			Id temp = (Id)exp;
			
			for(int i = 0; i < GVars.size(); ++i)
			{			
				if(temp.strVal.equals(GVars.get(i).name.strVal))
				{
					temp.type = GVars.get(i).type;
					this.type = temp.type;
					
					t = true;
					break;
				}
			}
			
			if(!t)
			{
				for(int i = 0; i < formal.size(); ++i)
				{
					if(temp.strVal.equals(formal.get(i).name.strVal))
					{
						temp.type = formal.get(i).type;
						this.type = temp.type;
						
						t = true;
						break;
					}
				}
			}
			
			if(!t)
			{
				for(int i = 0; i < vars.size(); ++i)
				{
					if(temp.strVal.equals(vars.get(i).name.strVal))
					{
						temp.type = vars.get(i).type;
						this.type = temp.type;
						
						t = true;
						break;
					}
				}
			}
			
			if(!t)
			{
				Errors.semanticError(temp.getLine(), temp.getChar(), "'" + temp.strVal + "' undeclared");
			}
		}
		else if(exp.getClass().toString().equals("class CallExp"))
		{
	    	CallExp temp = (CallExp)exp;
	    	temp.check(formal, vars, GVars, order, decl);
		}
		else if(exp.getClass().toString().equals("class IntLit"))
		{
			exp.type = Type.CreateSimpleType("int");
		}
		else if(exp.getClass().toString().equals("class StringLit"))
		{
			exp.type = Type.CreatePointerType("void", 1);
		}
	}
	
	public void checkType(Exp a, Exp b)
	{
		if((a.type != null && b.type != null) &&
				(a.type.name.compareTo(b.type.name) != 0 || a.type.numPointers != b.type.numPointers))
		{
			Errors.semanticWarn(b.getLine(), b.getChar(), "incompatible types");
		}
	}
	
	public void checkType(Exp a, Type b)
	{
		if(a.type != null)
		{
			if(a.type.name.compareTo(b.name) != 0 || a.type.numPointers != b.numPointers)
			{
				Errors.semanticWarn(a.getLine(), a.getChar(), "incompatible types");
			}
		}
	}
    
    protected Type type;
}

abstract class BasicExp extends Exp
{
    protected int lineNum;
    protected int charNum;
    
    public BasicExp(int lineNum, int charNum)
    {
        this.lineNum = lineNum;
        this.charNum = charNum;
    }
    
    public int getLine()
    {
        return lineNum;
    }
    
    public int getChar()
    {
        return charNum;
    }
}

class IntLit extends BasicExp {
    public IntLit(int lineNum, int charNum, int intVal) {
        super(lineNum, charNum);
        this.intVal = intVal;
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	this.type = Type.CreateSimpleType("int");
    }
    
    protected int intVal;
}

class StringLit extends BasicExp {
    public StringLit(int lineNum, int charNum, String strVal) {
        super(lineNum, charNum);
        this.strVal = strVal;
    }
    
    public String str() {
        return strVal;
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	this.type = Type.CreatePointerType("void", 1);
    }

    protected String strVal;
}

class Id extends BasicExp {
	public Id(int lineNum, int charNum, String strVal) {
		super(lineNum, charNum);
		this.strVal = strVal;
	}
	
	protected String strVal;
}

class ArrayExp extends Exp {
    public ArrayExp(Exp lhs, Exp exp) {
        this.lhs = lhs;
        this.exp = exp;
    }
    
    public int getLine() {
        return lhs.getLine();
    }

    public int getChar() {
        return lhs.getChar();
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(lhs, formal, vars, GVars, decl, order, type);
    	
    	boolean t = false;
		
		if(exp.getClass().toString().equals("class Id"))
		{
			Id temp = (Id)exp;
			
			for(int i = 0; i < GVars.size(); ++i)
			{			
				if(temp.strVal.equals(GVars.get(i).name.strVal))
				{
					temp.type = GVars.get(i).type;					
					t = true;
					break;
				}
			}
			
			if(!t)
			{
				for(int i = 0; i < formal.size(); ++i)
				{
					if(temp.strVal.equals(formal.get(i).name.strVal))
					{
						temp.type = formal.get(i).type;
						t = true;
						break;
					}
				}
			}
			
			if(!t)
			{
				for(int i = 0; i < vars.size(); ++i)
				{
					if(temp.strVal.equals(vars.get(i).name.strVal))
					{
						temp.type = vars.get(i).type;
						t = true;
						break;
					}
				}
			}
			
			if(!t)
			{
				Errors.semanticError(temp.getLine(), temp.getChar(), "'" + temp.strVal + "' undeclared");
			}
		}
		else if(exp.getClass().toString().equals("class CallExp"))
		{
	    	CallExp temp = (CallExp)exp;
	    	temp.check(formal, vars, GVars, order, decl);
		}
    }

    protected Exp lhs;
    protected Exp exp;
}

class CallExp extends Exp {
    public CallExp(Id name, ActualList actualList) {
        this.name = name;
        this.actualList = actualList;
    }
    
    public CallExp(Id name) {
        this.name = name;
        this.actualList = new ActualList(new LinkedList<Exp>());
    }

    public int getLine() {
        return name.getLine();
    }

    public int getChar() {
        return name.getChar();
    }

    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl)
    {
    	boolean pre = false;
    	
    	for(int i = 0; i <= order; ++i)
    	{
    		if(decl.get(i).name.strVal.equals(name.strVal) &&
    				(decl.get(i).getClass().toString().equals("class FnPreDecl") ||
    						decl.get(i).getClass().toString().equals("class FnDecl")))
    		{
    			this.type = decl.get(i).type;
    			pre = true;
    			break;
    		}
    	}
    	
    	actualList.check(formal, vars, GVars, order, decl);
    	
    	for(int i = 0; i <= order; ++i)
    	{
    		if(decl.get(i).name.strVal.equals(name.strVal) &&
    				decl.get(i).getClass().toString().equals("class FnPreDecl"))
			{
    			FnPreDecl temp = (FnPreDecl)decl.get(i);
    			
    			if(temp.formalList.formals.size() != actualList.exps.size())
				{
					Errors.semanticWarn(getLine(), getChar(), "prototype declaration");
				}
				else
				{
					for(int j = 0; j < actualList.exps.size(); ++j)
					{
						if(actualList.exps.get(j).type.name.compareTo(temp.formalList.formals.get(j).type.name) != 0)
						{
							Errors.semanticWarn(actualList.exps.get(j).getLine(), actualList.exps.get(j).getChar(),
									"prototype declaration");
						}
					}
				}
			}
    	}
    	
    	if(!pre)
    	{
    		Errors.semanticError(getLine(), getChar(), "undeclared function");
    	}
	}

    protected Id name;
    protected ActualList actualList;
}

class ActualList extends Ast {
    public ActualList(LinkedList<Exp> exps) {
        this.exps = exps;
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl)
    {
    	for(int i = 0; i < exps.size(); ++i)
    	{
    		boolean t = false;
    		
    		if(exps.get(i).getClass().toString().equals("class Id"))
    		{
    			Id temp = (Id)exps.get(i);
    			
    			for(int j = 0; j < GVars.size(); ++j)
    			{			
    				if(temp.strVal.equals(GVars.get(j).name.strVal))
    				{
    					temp.type = GVars.get(j).type;
    					t = true;
    					break;
    				}
    			}
    			
    			if(!t)
    			{
    				for(int j = 0; j < formal.size(); ++j)
    				{
    					if(temp.strVal.equals(formal.get(i).name.strVal))
    					{
    						temp.type = formal.get(i).type;
    						t = true;
    						break;
    					}
    				}
    			}
    			
    			if(!t)
    			{
    				for(int j = 0; j < vars.size(); ++j)
    				{
    					if(temp.strVal.equals(vars.get(i).name.strVal))
    					{
    						temp.type = vars.get(i).type;
    						t = true;
    						break;
    					}
    				}
    			}
    			
    			if(!t)
    			{
    				Errors.semanticError(temp.getLine(), temp.getChar(), "'" + temp.strVal + "' undeclared");
    			}
    		}
    		else if(exps.get(i).getClass().toString().equals("class CallExp"))
    		{
    	    	CallExp temp = (CallExp)exps.get(i);
    	    	temp.check(formal, vars, GVars, order, decl);
    		}
    	}
    }

    // linked list of kids (Exps)
    protected LinkedList<Exp> exps;
}

abstract class UnaryExp extends Exp {
    public UnaryExp(Exp exp) {
        this.exp = exp;
    }

    public int getLine() {
        return exp.getLine();
    }

    public int getChar() {
        return exp.getChar();
    }

    protected Exp exp;
}

abstract class BinaryExp extends Exp {
    public BinaryExp(Exp exp1, Exp exp2) {
        this.exp1 = exp1;
        this.exp2 = exp2;
    }

    public int getLine() {
        return exp1.getLine();
    }

    public int getChar() {
        return exp1.getChar();
    }
    
    protected Exp exp1;
    protected Exp exp2;
}


// **********************************************************************
// UnaryExps
// **********************************************************************
class UnaryMinusExp extends UnaryExp {
	public UnaryMinusExp(Exp exp) {
		super(exp);
    }
	
	public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp, formal, vars, GVars, decl, order, type);
    }
}

class NotExp extends UnaryExp {
    public NotExp(Exp exp) {
        super(exp);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp, formal, vars, GVars, decl, order, type);
    }
}

class AddrOfExp extends UnaryExp {
    public AddrOfExp(Exp exp) {
        super(exp);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp, formal, vars, GVars, decl, order, type);
    	
    	if(exp.type != null)
    	{
    		this.type.numPointers = exp.type.numPointers + 1;
    	}
    }
}

class DeRefExp extends UnaryExp {
    public DeRefExp(Exp exp) {
        super(exp);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp, formal, vars, GVars, decl, order, type);
    	
    	if(exp.type != null)
    	{
    		this.type.numPointers = exp.type.numPointers - 1;
    	}
    }
}

// **********************************************************************
// BinaryExps
// **********************************************************************
class PlusExp extends BinaryExp {
    public PlusExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}

class MinusExp extends BinaryExp {
    public MinusExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}

class TimesExp extends BinaryExp {
    public TimesExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}

class DivideExp extends BinaryExp {
    public DivideExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}

class ModuloExp extends BinaryExp {
    public ModuloExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}

class AndExp extends BinaryExp {
    public AndExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}

class OrExp extends BinaryExp {
    public OrExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}

class EqualsExp extends BinaryExp {
    public EqualsExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}

class NotEqualsExp extends BinaryExp {
    public NotEqualsExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}

class LessExp extends BinaryExp {
    public LessExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}

class GreaterExp extends BinaryExp {
    public GreaterExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}

class LessEqExp extends BinaryExp {
    public LessEqExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}

class GreaterEqExp extends BinaryExp {
    public GreaterEqExp(Exp exp1, Exp exp2) {
        super(exp1, exp2);
    }
    
    public void check(LinkedList<FormalDecl> formal, LinkedList<VarDecl> vars, LinkedList<VarDecl> GVars,
			int order, LinkedList<Decl> decl, Type type)
    {
    	checkUnDecl(exp1, formal, vars, GVars, decl, order, type);
    	checkUnDecl(exp2, formal, vars, GVars, decl, order, type);
    	
    	checkType(exp1, exp2);
    }
}