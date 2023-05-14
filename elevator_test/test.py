import subprocess
import time

# 读取指令文件instructions.txt
with open('instructions.txt', 'r') as f:
    instructions = f.readlines()

# 打开命令行，启动Main.jar程序
p = subprocess.Popen(['java', '-jar', 'Main.jar'], stdin=subprocess.PIPE, stdout=subprocess.PIPE)

# 依次输入指令并获取程序输出
for instruction in instructions:
    instruction = instruction.strip()  # 去除行末的换行符
    if instruction.startswith('['):
        # 如果是时间戳指令，暂停相应时间后再输入
        timestamp = float(instruction[1:instruction.index(']')])
        time.sleep(timestamp - time.time())  # 暂停到指定时间
        instruction = instruction[instruction.index(']')+1:].strip()  # 去除时间戳
    p.stdin.write((instruction + '\n').encode())  # 输入指令
    p.stdin.flush()  # 刷新输入缓存
    time.sleep(0.1)  # 等待程序处理完毕
    output = p.stdout.readline().decode().strip()  # 获取程序输出
    print(output)

# 关闭命令行和指令文件
p.stdin.close()
p.stdout.close()
