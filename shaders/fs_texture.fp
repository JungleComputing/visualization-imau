#version 150

uniform sampler2D my_texture;

uniform int scrWidth;
uniform int scrHeight;

void main() { 
	vec2 tCoord   = vec2(gl_FragCoord.x/float(scrWidth), gl_FragCoord.y/float(scrHeight));		
	vec4 color = vec4(texture(my_texture, tCoord).rgb, 1.0);
    gl_FragColor = vec4(color.rgb, 1.0);
} 

