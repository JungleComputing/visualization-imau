#version 150

uniform sampler2D my_texture;
//uniform sampler2D world_texture;
//uniform sampler2D mapping_texture;

uniform int scrWidth;
uniform int scrHeight;

out vec4 fragColor;

void main() { 
	vec2 tCoord   = vec2(gl_FragCoord.x/float(scrWidth), gl_FragCoord.y/float(scrHeight));
	
	//float map = texture(mapping_texture, tCoord).a;
	vec4 matColor; 
	//if (map < 0) {
	//	matColor = vec4(0,0,0,0);
	//} else {
		matColor = vec4(texture(my_texture, tCoord).rgb, 1.0);
	//}
	
	//vec4 worldColor = vec4(texture(world_texture, tCoord).rgb, 1.0);
	
	vec4 diffuse_factor;
	//if (matColor.r > 0.000001 || matColor.g > 0.000001 || matColor.b > 0.000001) {
		diffuse_factor = matColor;
	//} else {
	//	diffuse_factor = worldColor;
	//}
			
    fragColor = diffuse_factor;
} 

