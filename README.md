# HandwritingRecognition
This is a basic handwriting recognition module that makes use of David W. Arathorn's Map-Seeking Circuit for one-shot learning and recall of drawn patterns.

The user is presented with a space to draw characters, along with buttons to clear the draw space, attempt to recognize the drawn character, and to committ the drawn character to memory.

![image](https://user-images.githubusercontent.com/40635145/192380780-c1ad5089-496b-4958-8ed9-63f53129cea1.png)


### Preprocessing
The program takes in the locations of all the drawn pixels, and condenses them such that the "line density" remains constant. This decreases the variation from the speed at which characters are drawn.

Then, the centroid point is calculated, as well as the convex hull.

![image](https://user-images.githubusercontent.com/40635145/192383680-58e253ea-a1df-4f32-86a7-7b5cbeb32bbd.png)

Tangent line angles are calculated from the centroid point to a series of points along the convex hull, spaced equidistant from each other, with the total quantity being a power of 2 so that the data can be Fourier transformed. Finally, the cumulative angular function is calcuated and Fourier transformed.

This makes our data invariant under rotation, translation, and scaling. Therefore, it does not matter where the character is drawn, how big/small it is drawn, or even if it is drawn upside down - the program should be able to recognize it (see 2).

Each drawn character is passed to memory along with a label, and subsequent characters are compared. Upon recollection, the program displays which character in memory is a closest match, along with its confidence.

### References

1) Arathorn, D., 2002. Map-seeking circuits in visual cognition. Stanford, Calif.: Stanford University Press.

2) Zhang, D., & Lu, G., 2003. A comparative study of curvature scale space and Fourier descriptors for shape-based image retrieval. Journal of Visual Communication and Image Representation, 14(1), 39–57. https://doi.org/10.1016/s1047-3203(03)00003-8
