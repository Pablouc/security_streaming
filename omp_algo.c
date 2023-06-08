#include <stdio.h>
#include <stdlib.h>
#include <omp.h>

// Function to read a PPM image
unsigned char* readPpmImage(const char* filename, int* width, int* height)
{
    FILE* fp = fopen(filename, "rb");
    if (!fp) {
        fprintf(stderr, "Error opening file %s\n", filename);
        return NULL;
    }

    char magic[3];
    fscanf(fp, "%s", magic);
    if (magic[0] != 'P' || magic[1] != '6') {
        fprintf(stderr, "Invalid PPM format\n");
        fclose(fp);
        return NULL;
    }

    // Skip comments
    char c;
    while ((c = fgetc(fp)) == '#') {
        while (fgetc(fp) != '\n');
    }
    ungetc(c, fp);

    fscanf(fp, "%d %d", width, height);
    int maxVal;
    fscanf(fp, "%d%*c", &maxVal);
    
    int imageSize = (*width) * (*height) * 3;
    unsigned char* image = (unsigned char*)malloc(sizeof(unsigned char) * imageSize);

    fread(image, sizeof(unsigned char), imageSize, fp);
    fclose(fp);

    return image;
}

// Function to write a PPM image
void writePpmImage(const char* filename, const unsigned char* image, int width, int height)
{
    FILE* fp = fopen(filename, "wb");
    if (!fp) {
        fprintf(stderr, "Error opening file %s\n", filename);
        return;
    }

    fprintf(fp, "P6\n%d %d\n255\n", width, height);
    fwrite(image, sizeof(unsigned char), width * height * 3, fp);
    fclose(fp);
}

unsigned char* multifocusFusion(const unsigned char* image1, const unsigned char* image2, int width, int height)
{
    int imageSize = width * height * 3;
    unsigned char* fusedImage = (unsigned char*)malloc(sizeof(unsigned char) * imageSize);

    // Perform multifocus image fusion
    #pragma omp parallel for
    for (int i = 0; i < imageSize; i++) {
        // Simple averaging of pixel values from both images
        fusedImage[i] = (image1[i] + image2[i]) / 2;
    }

    return fusedImage;
}

int main()
{
    // Example usage
    int width = 1920;
    int height = 1200;
    unsigned char* image1 = readPpmImage("ppm/1left.ppm", &width, &height);
    unsigned char* image2 = readPpmImage("ppm/1right.ppm", &width, &height);

    if (!image1 || !image2) {
        return 1;
    }

    unsigned char* fusedImage = multifocusFusion(image1, image2, width, height);

    writePpmImage("fused_image.ppm", fusedImage, width, height);

    free(image1);
    free(image2);
    free(fusedImage);

    return 0;
}

