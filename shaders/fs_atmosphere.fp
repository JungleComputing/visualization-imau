#version 150

in vec3 vertex_pos;
in vec3 vertex_normal;

uniform float Shininess;

void main() {
	vec4 matColor = vec4(0.0, 1.0, 1.0, 1.0);
	
	vec3 light_direction = normalize(vec3(0.0, 0.0, 1.0));
	
	float dotP = dot(vertex_normal, light_direction);
	
	float diffuse_factor = max(1-dotP, 0.0);	

	gl_FragColor = vec4(1,1,1,diffuse_factor);
} 