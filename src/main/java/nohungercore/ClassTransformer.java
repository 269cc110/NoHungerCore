package nohungercore;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ClassTransformer implements IClassTransformer
{
    public static String trasnformedClass;

    @Override
    public byte[] transform(String s, String s1, byte[] bytes)
    {
        if (s1.equals("net.minecraft.entity.player.EntityPlayer"))
        {
            System.out.println("Patching: " + s1);
            trasnformedClass = s;
            return patchEntityPlayer(bytes);
        }
        else if (s1.equals("net.minecraftforge.client.GuiIngameForge"))
        {
            System.out.println("Patching: " + s1);
            trasnformedClass = s;
            return patchGUI(bytes);
        }

        return bytes;
    }

    private byte[] patchEntityPlayer(byte[] bytes)
    {
        ASMHelper.loadClass(bytes);

        //Change FoodStats to custom object
        ASMHelper.loadMethodInsn("<init>");
        ASMHelper.setTypeInsnRelOffset(Opcodes.NEW, "net/minecraft/util/FoodStats");
        ASMHelper.replace(new TypeInsnNode(Opcodes.NEW, "nohungercore/CustomFoodStats"));
        ASMHelper.skipUntil(Opcodes.DUP);
        ASMHelper.insert(new VarInsnNode(Opcodes.ALOAD, 0));
        ASMHelper.skipUntil(Opcodes.INVOKESPECIAL);
        ASMHelper.replace(new MethodInsnNode(Opcodes.INVOKESPECIAL, "nohungercore/CustomFoodStats", "<init>", "(Lnet/minecraft/entity/player/EntityPlayer;)V", false));
        ASMHelper.skipUntil(Opcodes.PUTFIELD);
        ASMHelper.replace(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/entity/player/EntityPlayer", SrgHandler.getString("foodStats"), "Lnet/minecraft/util/FoodStats;"));

        //Done!
        System.out.println("Done!");
        return ASMHelper.computeBytes();
    }

    private byte[] patchGUI(byte[] bytes)
    {
        ASMHelper.loadClass(bytes);

        //Remove hunger bar rendering
        ASMHelper.loadMethodInsn(SrgHandler.getString("renderGameOverlay"));
        ASMHelper.setRelOffset(LabelNode.class, 31);
        ASMHelper.removeInstructions(9);
        ASMHelper.setRelOffset(LabelNode.class, 30);
        ASMHelper.skipUntil(Opcodes.IFEQ);
        ASMHelper.fixJump();

        //Move the armor bar
        ASMHelper.loadMethodInsn("renderArmor");
        ASMHelper.setFieldInsnRelOffset(Opcodes.GETSTATIC, "left_height");
        ASMHelper.replace(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraftforge/client/GuiIngameForge", "right_height", "I"));
        ASMHelper.setOffset(Opcodes.BIPUSH, 1);
        ASMHelper.replace(new IntInsnNode(Opcodes.BIPUSH, 10));
        ASMHelper.skipUntil(Opcodes.ISUB);
        ASMHelper.replace(new InsnNode(Opcodes.IADD));

        //Fix air bar issue
        ASMHelper.setRelOffset(FrameNode.class, 6);
        ASMHelper.insert(new VarInsnNode(Opcodes.ILOAD, 5));
        ASMHelper.addJump(Opcodes.IFLE);
        ASMHelper.skipUntil(Opcodes.GETSTATIC);
        ASMHelper.replace(new FieldInsnNode(Opcodes.GETSTATIC, "net/minecraftforge/client/GuiIngameForge", "right_height", "I"));
        ASMHelper.skipUntil(Opcodes.PUTSTATIC);
        ASMHelper.replace(new FieldInsnNode(Opcodes.PUTSTATIC, "net/minecraftforge/client/GuiIngameForge", "right_height", "I"));
        ASMHelper.skipUntil(Opcodes.SIPUSH);
        ASMHelper.insertBefore(new FrameNode(Opcodes.F_SAME, 0, null, 0, null));

        //Done!
        System.out.println("Done!");
        return ASMHelper.computeBytes();
    }
}

