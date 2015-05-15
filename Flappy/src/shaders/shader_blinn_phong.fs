varying vec3 normal;
varying vec3 v; 
uniform vec3 lightDir; 
uniform vec3 ambCol; 
uniform vec3 specCol;
uniform vec3 diffCol;

void main (void)  
{  
	vec3 L = normalize(lightDir - v);   
	vec3 E = normalize(-v);  
	vec3 R = normalize(-reflect(L,normal));  

	//calculate Ambient Term:  
	float ambientStrength = 0.1;
	vec3 Iamb = ambientStrength * ambCol;

	//calculate Diffuse Term: 
	vec3 norm = normalize(normal);
	float diff = max(dot(norm, lightDir), 0.0);
	vec3 Idiff = diffCol * diff * v;
	Idiff = clamp(Idiff, 0.0, 1.0);

	//calculate Specular Term
	float specularShininess = 16.0;
	vec3 halfDir = normalize(lightDir + v);
	float specAngle = max(dot(halfDir,normal),0.0);
	vec3 Ispec = specCol * pow(specAngle,specularShininess);
	Ispec = clamp(Ispec, 0.0, 1.0);

	// write Total Color:  
	gl_FragColor = vec4(v + Iamb + Idiff + Ispec,0);     
}

