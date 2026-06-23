package com.leclowndu93150.thaumcraft.client.model.obj;

import org.joml.Vector3f;

public final class Material {
    private final String name;
    private Vector3f ambientColor;
    private Vector3f diffuseColor;
    private Vector3f specularColor;
    private float specularCoefficient;
    private float transparency;
    private int illuminationModel;
    private String ambientTextureMap;
    private String diffuseTextureMap;
    private String specularTextureMap;
    private String specularHighlightTextureMap;
    private String bumpMap;
    private String displacementMap;
    private String stencilDecalMap;
    private String alphaTextureMap;

    public Material(String name) {
        this.name = name;
    }

    public String name() { return name; }
    public Vector3f ambientColor() { return ambientColor; }
    public Vector3f diffuseColor() { return diffuseColor; }
    public Vector3f specularColor() { return specularColor; }
    public float specularCoefficient() { return specularCoefficient; }
    public float transparency() { return transparency; }
    public int illuminationModel() { return illuminationModel; }
    public String ambientTextureMap() { return ambientTextureMap; }
    public String diffuseTextureMap() { return diffuseTextureMap; }
    public String specularTextureMap() { return specularTextureMap; }
    public String specularHighlightTextureMap() { return specularHighlightTextureMap; }
    public String bumpMap() { return bumpMap; }
    public String displacementMap() { return displacementMap; }
    public String stencilDecalMap() { return stencilDecalMap; }
    public String alphaTextureMap() { return alphaTextureMap; }

    public void setAmbientColor(Vector3f v) { this.ambientColor = v; }
    public void setDiffuseColor(Vector3f v) { this.diffuseColor = v; }
    public void setSpecularColor(Vector3f v) { this.specularColor = v; }
    public void setSpecularCoefficient(float f) { this.specularCoefficient = f; }
    public void setTransparency(float f) { this.transparency = f; }
    public void setIlluminationModel(int i) { this.illuminationModel = i; }
    public void setAmbientTextureMap(String s) { this.ambientTextureMap = s; }
    public void setDiffuseTextureMap(String s) { this.diffuseTextureMap = s; }
    public void setSpecularTextureMap(String s) { this.specularTextureMap = s; }
    public void setSpecularHighlightTextureMap(String s) { this.specularHighlightTextureMap = s; }
    public void setBumpMap(String s) { this.bumpMap = s; }
    public void setDisplacementMap(String s) { this.displacementMap = s; }
    public void setStencilDecalMap(String s) { this.stencilDecalMap = s; }
    public void setAlphaTextureMap(String s) { this.alphaTextureMap = s; }
}
