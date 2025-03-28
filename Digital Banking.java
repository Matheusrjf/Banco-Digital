Arquivo: Cliente.java
```java
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Cliente {
    private String nome;
    private String cpf;
    private String senhaHash;
    private List<Conta> contas = new ArrayList<>();

    public Cliente(String nome, String cpf, String senha) {
        this.nome = nome;
        this.cpf = cpf;
        this.senhaHash = hashSenha(senha);
    }

    private String hashSenha(String senha) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(senha.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao criptografar senha");
        }
    }

    public boolean autenticar(String senha) {
        return senhaHash.equals(hashSenha(senha));
    }

    public void adicionarConta(Conta conta) {
        contas.add(conta);
    }

    public List<Conta> getContas() {
        return contas;
    }
}
```

Arquivo: Conta.java
```java
public abstract class Conta {
    protected double saldo;
    protected String numero;
    protected Cliente cliente;
    
    public Conta(String numero, Cliente cliente) {
        this.numero = numero;
        this.cliente = cliente;
        this.saldo = 0.0;
    }

    public void depositar(double valor) {
        saldo += valor;
        System.out.println("Depósito realizado. Novo saldo: " + saldo);
    }

    public boolean sacar(double valor) {
        if (valor > saldo) {
            System.out.println("Saldo insuficiente.");
            return false;
        }
        saldo -= valor;
        System.out.println("Saque realizado. Novo saldo: " + saldo);
        return true;
    }

    public boolean transferir(Conta destino, double valor) {
        if (sacar(valor)) {
            destino.depositar(valor);
            System.out.println("Transferência realizada para " + destino.getNumero());
            return true;
        }
        return false;
    }

    public String getNumero() {
        return numero;
    }
}
```

Arquivo: ContaReal.java
```java
public class ContaReal extends Conta {
    public ContaReal(String numero, Cliente cliente) {
        super(numero, cliente);
    }
}
```

Arquivo: ContaDolar.java
```java
public class ContaDolar extends Conta {
    private static final double COTACAO_DOLAR = 5.0;

    public ContaDolar(String numero, Cliente cliente) {
        super(numero, cliente);
    }
}
```

Arquivo: BancoDigital.java
```java
import java.util.*;

public class BancoDigital {
    private static List<Cliente> clientes = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("1. Cadastrar Cliente\n2. Acessar Conta\n3. Sair");
            int opcao = scanner.nextInt();
            scanner.nextLine();
            
            switch (opcao) {
                case 1 -> cadastrarCliente();
                case 2 -> acessarConta();
                case 3 -> System.exit(0);
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private static void cadastrarCliente() {
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("CPF: ");
        String cpf = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();
        Cliente cliente = new Cliente(nome, cpf, senha);
        clientes.add(cliente);
        System.out.println("Cliente cadastrado com sucesso!");
        
        System.out.println("Deseja criar uma conta? (1: Real, 2: Dólar, 0: Não)");
        int escolha = scanner.nextInt();
        scanner.nextLine();
        if (escolha == 1) {
            cliente.adicionarConta(new ContaReal(UUID.randomUUID().toString(), cliente));
            System.out.println("Conta em Real criada!");
        } else if (escolha == 2) {
            cliente.adicionarConta(new ContaDolar(UUID.randomUUID().toString(), cliente));
            System.out.println("Conta em Dólar criada!");
        }
    }

    private static void acessarConta() {
        System.out.print("CPF: ");
        String cpf = scanner.nextLine();
        System.out.print("Senha: ");
        String senha = scanner.nextLine();

        for (Cliente cliente : clientes) {
            if (cliente.autenticar(senha)) {
                System.out.println("Login bem-sucedido!");
                gerenciarConta(cliente);
                return;
            }
        }
        System.out.println("CPF ou senha inválidos.");
    }

    private static void gerenciarConta(Cliente cliente) {
        while (true) {
            System.out.println("1. Depositar\n2. Sacar\n3. Transferir\n4. Sair");
            int opcao = scanner.nextInt();
            scanner.nextLine();

            if (opcao == 4) break;

            if (cliente.getContas().isEmpty()) {
                System.out.println("Você não possui contas cadastradas.");
                return;
            }
            Conta conta = cliente.getContas().get(0);

            switch (opcao) {
                case 1 -> {
                    System.out.print("Valor do depósito: ");
                    double valor = scanner.nextDouble();
                    conta.depositar(valor);
                }
                case 2 -> {
                    System.out.print("Valor do saque: ");
                    double valor = scanner.nextDouble();
                    conta.sacar(valor);
                }
                case 3 -> {
                    System.out.print("Número da conta destino: ");
                    String destinoNumero = scanner.next();
                    System.out.print("Valor da transferência: ");
                    double valor = scanner.nextDouble();
                    for (Cliente c : clientes) {
                        for (Conta cDest : c.getContas()) {
                            if (cDest.getNumero().equals(destinoNumero)) {
                                conta.transferir(cDest, valor);
                                return;
                            }
                        }
                    }
                    System.out.println("Conta destino não encontrada.");
                }
            }
        }
    }
}
```
