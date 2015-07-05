package nohungercore;

public class ASMCrash extends RuntimeException
{
    public ASMCrash()
    {
        super("NoHungerMod had an issue performing ASM operations");
    }

    @Override
    public String getMessage()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Something went wrong during ASM operations\n");
        builder.append("Please give this log to the mod developer!\n\n");

        builder.append("############################\n");
        builder.append("##########BYTECODE##########\n");
        builder.append("############################\n\n");

        builder.append("Class: ").append(ClassTransformer.trasnformedClass).append("\n");
        builder.append("Bytecode: \n").append(ASMHelper.getByteCodeAsString()).append("\n\n");

        builder.append("############################\n");
        builder.append("########END BYTECODE########\n");
        builder.append("############################\n\n");

        builder.append(super.getMessage());

        return builder.toString();
    }
}
