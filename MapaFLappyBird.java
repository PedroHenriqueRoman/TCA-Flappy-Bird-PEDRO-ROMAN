import java.util.Scanner;

/**
 * MapaFLappyBird
 * 
 * /home/aluno/Downloads/TCA-20261111T132933Z-1-001/TCA/MapaFLappyBird.java
 */
public class MapaFLappyBird {

    public char MapaFLappyBird;

    public static Scanner TECLADO = new Scanner(System.in);

    public static char[][] construirMapaMatriz() {
        int linhas = 10;
        int colunas = 50;
        char[][] matrizMapa = new char[linhas][colunas];

        for (int i = 0; i < linhas; i++) {
            for (int j = 0; j < colunas; j++) {
                // 1. Cantos superiores e inferiores
                if ((i == 0 || i == linhas - 1) && (j == 0 || j == colunas - 1)) {
                    matrizMapa[i][j] = '+';
                }
                // 2. Linha do Teto e da base final
                else if (i == 0 || i == linhas - 1) {
                    matrizMapa[i][j] = '-';
                }
                // 3. Linha divisória do chão (onde o pássaro pisa)
                else if (i == linhas - 2) {
                    if (j == 0 || j == colunas - 1) {
                        matrizMapa[i][j] = '|';
                    } else {
                        matrizMapa[i][j] = '_';
                    }
                }
                // 4. Paredes laterais e o céu limpo
                else {
                    if (j == 0 || j == colunas - 1) {
                        matrizMapa[i][j] = '|';
                    } else {
                        matrizMapa[i][j] = ' ';
                    }
                }
            }
    }

    matrizMapa[4][7] = 'P';

    return matrizMapa;
}



public static void imprimirMatrizChar(char[][] mat){
    int linhas = mat.length;
    int colunas = mat[0].length;

    System.out.printf("\nMatriz: %d x %d\n", linhas, colunas);

   
    System.out.print("           ");

    System.out.printf("\n");

    for(int i = 0; i < linhas; i++){
 
        System.out.printf("linha %02d:  ", i);

        for(int j = 0; j < colunas; j++){

            System.out.printf("%c", mat[i][j]); 
        }
        System.out.println();
    }
}



    public static void main(String[] args) {

        char[][] matrizMapaFlappy;

        matrizMapaFlappy = construirMapaMatriz();

        imprimirMatrizChar(matrizMapaFlappy);

    }
    
}
