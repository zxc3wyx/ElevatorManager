import random


def generate_instructions(pm):
    insts = []

    # 随机生成 5 到 50 条指令
    num_instructions = random.randint(5, 50)

    # 记录已经生成的电梯 ID
    elevator_ids = set()

    # 根据到达模式生成乘客到达信息
    if pm == "Night":
        # 一次性全部到达，终点层都是底层
        for i in range(num_instructions):
            passenger_id = i + 1
            start_floor = random.randint(2, 20)
            end_floor = 1
            instruction_time = random.uniform(0, 50)
            inst = f"[{round(instruction_time, 1)}]{passenger_id}-FROM-{start_floor}-TO-{end_floor}"
            print(inst)
            insts.append((inst, random.uniform(1, 50)))
    elif pm == "Morning":
        # 到达间隔不超过 2s，起始层都是底层
        for i in range(num_instructions):
            passenger_id = i + 1
            start_floor = 1
            end_floor = random.randint(2, 20)
            instruction_time = random.uniform(0, 50)
            inst = f"[{round(instruction_time, 1)}]{passenger_id}-FROM-{start_floor}-TO-{end_floor}"
            print(inst)
            insts.append((inst, instruction_time))

    else:
        # 允许任何到达情况出现
        for i in range(num_instructions):
            passenger_id = i + 1
            start_floor = random.randint(1, 20)
            end_floor = random.randint(1, 20)
            while end_floor == start_floor:
                end_floor = random.randint(1, 20)
            instruction_time = random.uniform(0, 50)
            inst = f"[{round(instruction_time, 1)}]{passenger_id}-FROM-{start_floor}-TO-{end_floor}"
            print(inst)
            insts.append((inst, instruction_time))

    # 随机生成 0 到 3 次添加电梯的请求
    num_elevators = random.randint(0, 3)
    for i in range(num_elevators):
        elevator_id = random.randint(1, 2147483647)
        while elevator_id in elevator_ids:
            elevator_id = random.randint(1, 2147483647)
        elevator_ids.add(elevator_id)
        elevator_type = random.choice(["A", "B", "C"])
        instruction_time = random.uniform(1, 50)
        inst = f"[{instruction_time:.1f}]ADD-{elevator_id}-{elevator_type}"
        insts.append((inst, instruction_time))

    # 按照时间戳排序
    insts = sorted(insts, key=lambda x: x[1])
    return insts


# 生成指令并保存到文件
if __name__ == "__main__":
    passenger_mode = random.choice(["Night", "Morning", "Random"])
    instructions = generate_instructions(passenger_mode)
    # Save instructions to file
    with open('instructions.txt', 'w') as f:
        f.write(passenger_mode + '\n')
        for instruction in instructions:
            f.write(str(instruction[0]) + '\n')
