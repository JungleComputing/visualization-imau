#version 150

in vec3 vertex_pos;
in vec3 vertex_normal;

void main() {
	vec3 matColor = vec3(0.3, 0.6, 1.0);
	
	vec3 eye_direction = normalize(-vertex_pos);
	
	float dotP = dot(vertex_normal, eye_direction);
	
	float diffuse_factor = max(.5-dotP, 0.0);	

	gl_FragColor = vec4(matColor,diffuse_factor);
} 