#version 330

#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

in vec3 Position;

out vec3 eyePos;

void main() {
    vec4 eye = ModelViewMat * vec4(Position, 1.0);
    eyePos = eye.xyz;
    gl_Position = ProjMat * eye;
}
