package nohungercore;


import cpw.mods.fml.common.FMLCommonHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;

public class ASMHelper
{
    private static ClassNode classNode;
    private static InsnList insnList;
    private static AbstractInsnNode lastMod;
    private static Printer printer = new Textifier();
    private static TraceMethodVisitor mp = new TraceMethodVisitor(printer);

    public static void loadClass(byte[] bytes)
    {
        classNode = new ClassNode();
        ClassReader reader = new ClassReader(bytes);
        reader.accept(classNode, 0);
    }

    public static void loadMethodInsn(String methodName)
    {
        for (MethodNode methodNode : classNode.methods)
        {
            if (methodNode.name.equals(methodName))
            {
                System.out.println("Found method: " + methodName);
                insnList = methodNode.instructions;
                lastMod = null;
                return;
            }
        }

        throwException();
    }

    public static byte[] computeBytes()
    {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(writer);
        byte[] result =  writer.toByteArray();

        classNode = null;
        insnList = null;
        lastMod = null;

        return result;
    }

    public static void skipInstruction()
    {
        lastMod = lastMod.getNext();
    }

    public static void skipUntil(int opcode)
    {
        while (lastMod.getOpcode() != opcode)
        {
            lastMod = lastMod.getNext();
        }
    }

    public static void setOffset(int opcode, int count)
    {
        Iterator<AbstractInsnNode> iter = insnList.iterator();

        int count2 = 1;

        while (iter.hasNext())
        {
            AbstractInsnNode node = iter.next();

            if (node.getOpcode() == opcode)
            {
                if (count2 == count)
                {
                    lastMod = node;
                    return;
                }

                count2++;
            }
        }
    }

    public static void setRelOffset(Class<? extends AbstractInsnNode> c, int count)
    {
        Iterator<AbstractInsnNode> iter = insnList.iterator();

        int count2 = 1;

        while (iter.hasNext())
        {
            AbstractInsnNode node = iter.next();

            if (c.isAssignableFrom(node.getClass()))
            {
                if (count == count2)
                {
                    lastMod = node;
                    return;
                }

                count2++;
            }
        }
    }

    public static void setTypeInsnRelOffset(int opcode, String desc)
    {
        Iterator<AbstractInsnNode> iter = insnList.iterator();

        while (iter.hasNext())
        {
            AbstractInsnNode node = iter.next();

            if (node.getOpcode() == opcode && node instanceof TypeInsnNode)
            {
                if (((TypeInsnNode) node).desc.equals(desc))
                {
                    lastMod = node;
                    return;
                }
            }
        }
    }

    public static void setFieldInsnRelOffset(int opcode, String name)
    {
        Iterator<AbstractInsnNode> iter = insnList.iterator();

        while (iter.hasNext())
        {
            AbstractInsnNode node = iter.next();

            if (node.getOpcode() == opcode && node instanceof FieldInsnNode)
            {
                if (((FieldInsnNode) node).name.equals(name))
                {
                    lastMod = node;
                    return;
                }
            }
        }
    }

    public static void insertBefore(AbstractInsnNode node)
    {
        insnList.insertBefore(lastMod, node);
    }

    public static void replace(AbstractInsnNode node)
    {
        insnList.set(lastMod, node);

        lastMod = node;
    }

    public static void insert(AbstractInsnNode node)
    {
        insnList.insert(lastMod, node);
        lastMod = node;
    }

    public static void addJump(int opcode)
    {
        AbstractInsnNode lbl = lastMod.getNext();

        while (!(lbl instanceof LabelNode))
        {
            lbl = lbl.getNext();
        }

        insert(new JumpInsnNode(opcode, (LabelNode) lbl));
    }

    public static void removeInstructions(int count)
    {
        for (int i = 0; i < count; i++)
        {
            AbstractInsnNode copy = lastMod.getNext();
            insnList.remove(lastMod);
            lastMod = copy;
        }
    }

    public static void fixJump()
    {
        AbstractInsnNode n = lastMod.getNext();

        while (!(n instanceof LabelNode))
        {
            n = n.getNext();
        }

        ((JumpInsnNode) lastMod).label = (LabelNode) n;
    }

    public static String getByteCodeAsString()
    {
        StringWriter sw = new StringWriter();

        try
        {
            insnList.accept(mp);
            printer.print(new PrintWriter(sw));
            printer.getText().clear();
        }
        catch (Exception e) {}

        return sw.toString();
    }

    private static void throwException()
    {
        FMLCommonHandler.instance().raiseException(new ASMCrash(), "NoHungerMod had an issue performing ASM operations", true);
    }
}
