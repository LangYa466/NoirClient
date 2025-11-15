package net.optifine.shaders.config;

public record ShaderMacro(String name, String value) {

    public String getSourceLine() {
        return "#define " + this.name + " " + this.value;
    }

    public String toString() {
        return this.getSourceLine();
    }
}
