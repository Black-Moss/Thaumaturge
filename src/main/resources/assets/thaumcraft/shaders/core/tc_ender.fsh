#version 330

#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:globals.glsl>

#define M_PI 3.1415926535897932384626433832795

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec3 eyePos;

out vec4 fragColor;

mat4 rotationMatrix(vec3 axis, float angle) {
    axis = normalize(axis);
    float s = sin(angle);
    float c = cos(angle);
    float oc = 1.0 - c;

    return mat4(oc * axis.x * axis.x + c,           oc * axis.x * axis.y - axis.z * s,  oc * axis.z * axis.x + axis.y * s,  0.0,
                oc * axis.x * axis.y + axis.z * s,  oc * axis.y * axis.y + c,           oc * axis.y * axis.z - axis.x * s,  0.0,
                oc * axis.z * axis.x - axis.y * s,  oc * axis.y * axis.z + axis.x * s,  oc * axis.z * axis.z + c,           0.0,
                0.0,                                0.0,                                0.0,                                1.0);
}

void main() {
    vec4 col = vec4(0.044, 0.036, 0.063, 1.0);

    float time = GameTime * 24000.0;

    vec3 eyeDir = normalize(-eyePos);
    vec4 dir = vec4(normalize(transpose(mat3(ModelViewMat)) * eyeDir), 0.0);

    vec4 ray;

    for (int i = 0; i < 16; i++) {
        int mult = 16 - i;

        int j = i + 7;
        float rand1 = float(j * j * 4321 + j * 8) * 2.0;
        int k = j + 1;
        float rand2 = float(k * k * k * 239 + k * 37) * 3.6;
        float rand3 = rand1 * 347.4 + rand2 * 63.4;

        vec3 axis = normalize(vec3(sin(rand1), sin(rand2), cos(rand3)));

        ray = dir * rotationMatrix(axis, mod(rand3, 2.0 * M_PI));

        float u = 0.5 + (atan(ray.z, ray.x) / (2.0 * M_PI));
        float v = 0.5 + (asin(clamp(ray.y, -1.0, 1.0)) / M_PI);

        float scale = float(mult) * 0.5 + 2.75;
        vec2 tex = vec2(u * scale, (v + time * 0.00006) * scale * 0.6);

        vec4 tcol = texture(Sampler0, tex);

        float a = tcol.r * (0.05 + (1.0 / float(mult)) * 0.65) * (1.0 - smoothstep(0.15, 0.48, abs(v - 0.5)));

        float r = (mod(rand1, 29.0) / 29.0) * 0.5 + 0.1;
        float g = (mod(rand2, 35.0) / 35.0) * 0.5 + 0.4;
        float b = (mod(rand1, 17.0) / 17.0) * 0.5 + 0.5;

        col = col * (1.0 - a) + vec4(r, g, b, 1.0) * a;
    }

    fragColor = vec4(col.rgb, 1.0);
}
