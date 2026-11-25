import java.util.Scanner; // Ló
import java.util.Random;
import com.sun.jna.Library; // Ló
import com.sun.jna.Native; // Ló

public class FlappyBird_PedroRenato {

    public final static Scanner TECLADO = new Scanner(System.in); // Ló

    static Random random = new Random();

    static int largura = 50; // Ló
    static int altura = 10; // Ló

    static int posX = 35; // Ló
    static int posY = 5; // Ló

    static int posicaoBarreira = largura - 2;
    static int abertura = random.nextInt(5) + 1;

    static int barreirasPassadas = 0;
    static int contadorVelocidade = 0;

    // =========================================
    // CONTROLE DO TECLADO
    // =========================================

    // Ló
    public interface Crt extends Library { // Permite acessar funções nativas do Windows utilizando a biblioteca JNA.

        Crt INSTANCE = Native.load("msvcrt", Crt.class);

        int _kbhit(); // Verifica se existe uma tecla disponível para leitura.

        int _getch(); // Captura a tecla pressionada sem precisar apertar Enter.
    }

    // Ló
    static boolean windows = System.getProperty("os.name")
            .toLowerCase().contains("win");

    static boolean tecladoPreparado = false; // Ló

    // Ló
    public static boolean pressionouTecla() { // Verifica se o jogador pressionou alguma tecla sem interromper a execução do jogo.

        if (windows) {
            return Crt.INSTANCE._kbhit() != 0;
        }

        prepararTecladoMacLinux();

        try {
            return System.in.available() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Ló
    public static int obtemTeclaPressionada() { // Captura e retorna o código numérico da tecla pressionada pelo jogador.

        if (windows) {
            return Crt.INSTANCE._getch();
        }

        prepararTecladoMacLinux();

        try {
            return System.in.read();
        } catch (Exception e) {
            return -1;
        }
    }

    // Ló
    public static void esperarTecla() { // Aguarda o jogador pressionar uma tecla para iniciar o jogo.

        System.out.println("Pressione uma tecla para iniciar...");
        obtemTeclaPressionada();
    }

    // Ló
    static void prepararTecladoMacLinux() { // Configura o terminal do Mac e Linux para capturar teclas sem precisar apertar Enter.

        if (tecladoPreparado) {
            return;
        }

        tecladoPreparado = true;

        executarStty("-icanon -echo -icrnl min 1");

        Runtime.getRuntime().addShutdownHook(
            new Thread(() -> executarStty("sane"))
        );
    }

    // Ló
    static void executarStty(String opcoes) { // Executa comandos do sistema operacional para modificar as configurações do terminal.

        try {
            new ProcessBuilder(
                "sh", "-c", "stty " + opcoes + " < /dev/tty"
            ).start().waitFor();

        } catch (Exception e) {
        }
    }

    // =========================================
    // FUNCOES AUXILIARES
    // =========================================

    // Ló
    public static void limparConsole() { // Limpa o console para atualizar o cenário e criar a sensação de movimento.

        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    // Ló
    public static void esperar(int milissegundos) { // Pausa temporariamente a execução do programa para controlar a velocidade do jogo.

        try {
            Thread.sleep(milissegundos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // =========================================
    // CONSTRUIR MAPA
    // =========================================

    public static char[][] construirMapaMatriz(int posX, int posY) { // Cria uma matriz de caracteres que representa o cenário, suas bordas, o chão e o personagem.

        char[][] matrizMapa = new char[altura][largura];

        for (int i = 0; i < altura; i++) {

            for (int j = 0; j < largura; j++) {

                if ((i == 0 || i == altura - 1)
                        && (j == 0 || j == largura - 1)) {

                    matrizMapa[i][j] = '+';
                }

                else if (i == 0 || i == altura - 1) {

                    matrizMapa[i][j] = '-';
                }

                else if (i == altura - 2) {

                    if (j == 0 || j == largura - 1) {
                        matrizMapa[i][j] = '|';
                    } else {
                        matrizMapa[i][j] = '_';
                    }
                }

                else {

                    if (j == 0 || j == largura - 1) {
                        matrizMapa[i][j] = '|';
                    } else {
                        matrizMapa[i][j] = ' ';
                    }
                }
            }
        }

        matrizMapa[posY][posX] = 'P';

        return matrizMapa;
    }

    // =========================================
    // CRIAR BARREIRA
    // =========================================

    public static void criarBarreira(char[][] mapa, int posicao, int abertura) { // Cria uma barreira utilizando o caractere #, deixando uma abertura de três espaços para o jogador passar.

        for (int i = 1; i < 8; i++) {

            if (i < abertura || i > abertura + 2) {

                mapa[i][posicao] = '#';
            }
        }
    }

    // =========================================
    // MOVIMENTAR BARREIRA
    // =========================================

    public static void movimentarBarreira() { // Movimenta a barreira uma coluna para a esquerda a cada três chamadas do método.

        contadorVelocidade++;

        if (contadorVelocidade >= 3) {

            posicaoBarreira--;

            contadorVelocidade = 0;
        }
    }

    // =========================================
    // CONTAR BARREIRAS
    // =========================================

    public static void contarBarreiras() { // Conta as barreiras ultrapassadas e reposiciona a barreira com uma nova abertura aleatória.

        if (posicaoBarreira < posX) {

            barreirasPassadas++;

            posicaoBarreira = largura - 2;

            abertura = random.nextInt(5) + 1;
        }
    }

    // =========================================
    // MOVIMENTAR PERSONAGEM
    // =========================================

    public static void movimentarPersonagem() { // Controla o movimento do personagem com W e S, permite sair com ESC e impede que ele ultrapasse os limites do cenário.

        // Ló
        if (pressionouTecla()) {

            int tecla = obtemTeclaPressionada();

            switch (tecla) {

                case 'w':
                case 'W':
                    posY--;
                    break;

                case 's':
                case 'S':
                    posY++;
                    break;

                case 27: 
                    imprimirTelaFinal();
                    esperar(3000);
                    System.exit(0);
            }
        }

        // Ló
        if (posY < 1) {
            posY = 1;
        }

        // Ló
        if (posY > altura - 3) {
            posY = altura - 3;
        }
    }

    // =========================================
    // VERIFICAR COLISAO
    // =========================================

    public static boolean verificarColisao() { // Verifica se o personagem está na mesma coluna da barreira e fora da abertura, identificando uma colisão.

        if (posicaoBarreira == posX) {

            if (posY < abertura || posY > abertura + 2) {

                return true;
            }
        }

        return false;
    }

    // =========================================
    // VERIFICAR VITORIA
    // =========================================

    public static boolean verificarVitoria() { // Verifica se o jogador atingiu o objetivo de ultrapassar 50 barreiras.

        return barreirasPassadas >= 50;
    }

    // =========================================
    // IMPRIMIR MAPA
    // =========================================

    public static void imprimirMatrizChar(char[][] mat) { // Percorre a matriz utilizando dois laços de repetição e imprime cada caractere do cenário no console.

        for (int i = 0; i < mat.length; i++) {

            for (int j = 0; j < mat[i].length; j++) {

                System.out.print(mat[i][j]);
            }

            System.out.println();
        }

        System.out.flush();
    }

    // =========================================
    // DESENHAR JOGO
    // =========================================

    public static void desenharJogo() { // Reúne os métodos de construção do mapa e da barreira, atualiza o console e exibe a pontuação do jogador.

        char[][] matrizMapa = construirMapaMatriz(posX, posY);

        if (posicaoBarreira > 0 && posicaoBarreira < largura - 1) {

            criarBarreira(matrizMapa, posicaoBarreira, abertura);
        }

        limparConsole(); // Ló

        imprimirMatrizChar(matrizMapa);

        System.out.println();
        System.out.println("Barreiras ultrapassadas: "
                + barreirasPassadas + "/50");
    }

    // =========================================
    // ASCII ART DERROTA
    // =========================================

    public static void imprimirTelaFinal() { // Limpa o console e exibe uma mensagem de derrota utilizando uma arte formada por caracteres ASCII.

        limparConsole(); // Ló

        System.out.println("____  _____ ____  ____  _____ _   _   _   _   _");
        System.out.println("|  _ \\| ____|  _ \\|  _ \\| ____| | | | | | | | | |");
        System.out.println("| |_) |  _| | |_) | | | |  _| | | | | | | | | |");
        System.out.println("|  __/| |___|  _ <| |_| | |___| |_| |_| |_| |_|");
        System.out.println("|_|_  |_____|_| \\_\\____/|_____\\_____/ (_) (_) (_)");
        System.out.println("  | |/ / |/ / |/ / |/ / |/ /");
        System.out.println("  | ' /| ' /| ' /| ' /| ' /");
        System.out.println("  | . \\| . \\| . \\| . \\| . \\");
        System.out.println("  |_|\\_\\_|\\_\\_|\\_\\_|\\_\\_|\\_\\");
    }

    // =========================================
    // ASCII ART VITORIA
    // =========================================

    public static void imprimirVitoria() { // Limpa o console e apresenta uma mensagem de parabéns quando o jogador completa o objetivo do jogo.

        limparConsole(); // Ló

        System.out.println(" ____                 _                                       \r\n" + //
                        "|  _ \\ __ _ _ __ __ _| |__   ___ _ __  ___                    \r\n" + //
                        "| |_) / _` | '__/ _` | '_ \\ / _ \\ '_ \\/ __|                   \r\n" + //
                        "|  __/ (_| | | | (_| | |_) |  __/ | | \\__ \\                   \r\n" + //
                        "|_|   \\__,_|_|  \\__,_|_.__/ \\___|_| |_|___/             _ _ _ \r\n" + //
                        "__   _____   ___ ___  __   _____ _ __   ___ ___ _   _  | | | |\r\n" + //
                        "\\ \\ / / _ \\ / __/ _ \\ \\ \\ / / _ \\ '_ \\ / __/ _ \\ | | | | | | |\r\n" + //
                        " \\ V / (_) | (_|  __/  \\ V /  __/ | | | (_|  __/ |_| | |_|_|_|\r\n" + //
                        "  \\_/ \\___/ \\___\\___|   \\_/ \\___|_| |_|\\___\\___|\\__,_| (_|_|_)");

        System.out.println();
        System.out.println("Obrigado por jogar!");
    }

    // =========================================
    // MAIN
    // =========================================

    public static void main(String[] args) { // Método principal responsável por iniciar o jogo e executar os demais métodos continuamente até ocorrer uma vitória ou derrota.

        limparConsole(); // Ló

        System.out.println("\t\t===============\n\t\t| FLAPPY BIRD |\n\t\t===============\n");
        System.out.println("\t\tUse W e S para movimentar.\n"); // Ló
        System.out.println("\t\tPressione ESC para sair.\n"); // Ló

        esperarTecla(); // Ló

        while (true) { // Ló

            movimentarPersonagem(); // Ló

            movimentarBarreira();

            contarBarreiras();

            if (verificarColisao()) {

                imprimirTelaFinal();
                esperar(3000);
                break;
            }

            if (verificarVitoria()) {

                imprimirVitoria();
                esperar(5000);
                break;
            }

            desenharJogo();

            esperar(10); // Ló
        }
    }
}
